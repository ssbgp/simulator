package simulation

import bgp.BGP
import bgp.BGPRoute
import core.routing.NodeID
import core.routing.Topology
import core.simulator.Advertisement
import core.simulator.RandomDelayGenerator
import core.simulator.Time
import io.InterdomainAdvertisementReader
import io.InterdomainTopologyReader
import io.ParseException
import io.parseInterdomainExtender
import ui.Application
import java.io.File
import java.io.IOException

/**
 * Created on 09-11-2017
 *
 * @author David Fialho
 */
sealed class BGPAdvertisementInitializer(
        // Mandatory
        val topologyFile: File,

        // Optional (with defaults)
        var repetitions: Int = DEFAULT_REPETITIONS,
        var minDelay: Time = DEFAULT_MINDELAY,
        var maxDelay: Time = DEFAULT_MAXDELAY,
        var threshold: Time = DEFAULT_THRESHOLD,
        var reportDirectory: File = DEFAULT_REPORT_DIRECTORY,
        var reportNodes: Boolean = false,
        var outputMetadata: Boolean = false,
        var outputTrace: Boolean = false,

        // Optional (without defaults)
        var seed: Long? = null,
        var stubsFile: File? = null,
        var forcedMRAI: Time? = null

): Initializer<BGPRoute> {

    companion object {

        val DEFAULT_REPETITIONS = 1
        val DEFAULT_THRESHOLD = 1_000_000
        val DEFAULT_MINDELAY = 1
        val DEFAULT_MAXDELAY = 1
        val DEFAULT_REPORT_DIRECTORY = File(System.getProperty("user.dir"))  // current working directory

        fun with(topologyFile: File, advertiserIDs: Set<NodeID>): BGPAdvertisementInitializer =
                BGPAdvertisementInitializer.UsingDefaultSet(topologyFile, advertiserIDs)

        fun with(topologyFile: File, advertisementsFile: File): BGPAdvertisementInitializer =
                BGPAdvertisementInitializer.UsingFile(topologyFile, advertisementsFile)
    }

    /**
     * This is the base output name for the report files. The base output name does no include the extension.
     * Subclasses should provide a name depending on their specifications.
     */
    abstract val outputName: String

    /**
     * Initializes a simulation. It sets up the executions to run and the runner to run them.
     */
    override fun initialize(application: Application, metadata: Metadata): Pair<Runner<BGPRoute>, Execution<BGPRoute>> {

        // If no seed is set, then a new seed is generated, based on the current time, for each new initialization
        val seed = seed ?: System.currentTimeMillis()

        // Append extensions according to the file type
        val basicReportFile = File(reportDirectory, outputName.plus(".basic.csv"))
        val nodesReportFile = File(reportDirectory, outputName.plus(".nodes.csv"))
        val metadataFile = File(reportDirectory, outputName.plus(".meta.txt"))
        val traceFile = File(reportDirectory, outputName.plus(".trace.txt"))

        // Setup the message delay generator
        val messageDelayGenerator = try {
            RandomDelayGenerator.with(minDelay, maxDelay, seed)
        } catch (e: IllegalArgumentException) {
            throw InitializationException(e.message)
        }

        // Load the topology
        val topology: Topology<BGPRoute> = application.loadTopology(topologyFile) {
            InterdomainTopologyReader(topologyFile, forcedMRAI).use {
                it.read()
            }
        }

        val advertisements = application.setupAdvertisements {
            // Subclasses determine how advertisements are configured, see subclasses at the bottom of this file
            initAdvertisements(application, topology)
        }

        val runner = RepetitionRunner(
                application,
                topology,
                advertisements,
                threshold,
                repetitions,
                messageDelayGenerator,
                metadataFile = if(outputMetadata) metadataFile else null  // null tells the runner not to print metadata
        )

        val execution = SimpleAdvertisementExecution<BGPRoute>().apply {
            dataCollectors.add(BasicDataCollector(basicReportFile))

            if (reportNodes) {
                dataCollectors.add(NodeDataCollector(nodesReportFile))
            }

            if (outputTrace) {
                dataCollectors.add(TraceReporter(traceFile))
            }
        }

        metadata["Topology file"] = topologyFile.name
        stubsFile?.apply {
            metadata["Stubs file"] = name
        }
        metadata["Advertiser(s)"] = advertisements.map { it.advertiser.id }.joinToString()
        metadata["Minimum Delay"] = minDelay.toString()
        metadata["Maximum Delay"] = maxDelay.toString()
        metadata["Threshold"] = threshold.toString()
        forcedMRAI?.apply {
            metadata["MRAI"] = forcedMRAI.toString()
        }

        return Pair(runner, execution)
    }

    /**
     * Subclasses should use this method to initialize advertisements to occur in the simulation. The way these are
     * defined is dependent on the implementation.
     *
     * @return list of initialized advertisements to occur in the simulation
     */
    protected abstract fun initAdvertisements(application: Application, topology: Topology<BGPRoute>)
            : List<Advertisement<BGPRoute>>

    // -----------------------------------------------------------------------------------------------------------------
    //
    //  Subclasses
    //
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Initialization based on a set of pre-defined advertiser IDs. Each ID is mapped to an advertiser. An advertiser
     * can be obtained from the topology or from a stubs file.
     *
     * It generates a single advertisement for each advertiser, with a default route corresponding to the self BGP
     * route, and an advertising time of 0.
     */
    private class UsingDefaultSet(topologyFile: File, val advertiserIDs: Set<NodeID>)
        : BGPAdvertisementInitializer(topologyFile) {

        init {
            // Verify that at least 1 advertiser ID is provided in the constructor
            if (advertiserIDs.isEmpty()) {
                throw IllegalArgumentException("initializer requires at least 1 advertiser")
            }
        }

        /**
         * The output name (excluding the extension) corresponds to the topology filename and the IDs of the
         * advertisers. For instance, if the topology file name is `topology.topo` and the advertiser IDs are 10 and
         * 12, then the output file name will be `topology_10-12`.
         */
        override val outputName: String = topologyFile.nameWithoutExtension + "_${advertiserIDs.joinToString("-")}"

        /**
         * A single advertisement is created for each advertiser specified in the ID set.
         *
         * @throws InitializationException if the advertisers can not be found in the topology or stubs file
         * @throws ParseException if the stubs file format is invalid
         * @throws IOException if an IO error occurs
         * @return list of initialized advertisements to occur in the simulation
         */
        override fun initAdvertisements(application: Application, topology: Topology<BGPRoute>)
                : List<Advertisement<BGPRoute>> {

            // Find all the advertisers from the specified IDs
            val advertisers = application.readStubsFile(stubsFile) {
                AdvertiserDB(topology, stubsFile, BGP(), ::parseInterdomainExtender)
                        .get(advertiserIDs.toList())
            }

            // In this mode, nodes set the self BGP route as the default route
            // Use the advertisements file to configure different routes
            return advertisers.map { Advertisement(it, BGPRoute.self()) }.toList()
        }
    }

    /**
     * Initialization based on an advertisements file. This file describes the set of advertisements to occur in each
     * simulation execution.
     *
     * The advertiser IDs described in the advertisements file are mapped to actual advertisers. An advertiser
     * can be obtained from the topology or from a stubs file.
     */
    private class UsingFile(topologyFile: File, val advertisementsFile: File)
        : BGPAdvertisementInitializer(topologyFile) {

        /**
         * The output name (excluding the extension) corresponds to the topology filename appended with the
         * advertisements file name.
         *
         * For instance, if the topology file name is `topology.topo` and the advertisements file name is
         * `advertisements.adv`, then the output base name is `topology-advertisements`
         */
        override val outputName: String =
                "${topologyFile.nameWithoutExtension}-${advertisementsFile.nameWithoutExtension}"

        /**
         * Advertisements are obtained from an advertisements file
         *
         * @return list of initialized advertisements to occur in the simulation
         * @throws InitializationException if the advertisers can not be found in the topology or stubs file
         * @throws ParseException if the advertisements file format or the stubs file format are invalid
         * @throws IOException if an IO error occurs
         */
        @Throws(InitializationException::class, ParseException::class, IOException::class)
        override fun initAdvertisements(application: Application, topology: Topology<BGPRoute>)
                : List<Advertisement<BGPRoute>> {

            val advertisingInfo = application.readAdvertisementsFile(advertisementsFile) {
                InterdomainAdvertisementReader(advertisementsFile).use {
                    it.read()
                }
            }

            // Find all the advertisers based on the IDs included in the advertisements file
            val advertisers = application.readStubsFile(stubsFile) {
                AdvertiserDB(topology, stubsFile, BGP(), ::parseInterdomainExtender)
                        .get(advertisingInfo.map { it.advertiserID })
            }

            val advertisersByID = advertisers.associateBy { it.id }

            return advertisingInfo.map {
                val advertiser = advertisersByID[it.advertiserID] ?: throw IllegalStateException("can not happen")
                Advertisement(advertiser, it.defaultRoute, it.time)
            }
        }
    }
}