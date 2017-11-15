package simulation

import bgp.BGP
import bgp.BGPRoute
import core.routing.NodeID
import core.routing.Topology
import core.simulator.Advertisement
import core.simulator.RandomDelayGenerator
import core.simulator.Time
import io.AdvertisementInfo
import io.InterdomainAdvertisementReader
import io.InterdomainTopologyReader
import io.parseInterdomainExtender
import ui.Application
import java.io.File

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

        // Optional (without defaults)
        var seed: Long? = null,
        var stubsFile: File? = null

): Initializer<BGPRoute> {

    companion object {

        val DEFAULT_REPETITIONS = 1
        val DEFAULT_THRESHOLD = 1_000_000
        val DEFAULT_MINDELAY = 1
        val DEFAULT_MAXDELAY = 1
        val DEFAULT_REPORT_DIRECTORY = File(System.getProperty("user.dir"))  // current working directory

        fun with(topologyFile: File, advertiserIDs: List<NodeID>): BGPAdvertisementInitializer =
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

        // Setup the message delay generator
        val messageDelayGenerator = try {
            RandomDelayGenerator.with(minDelay, maxDelay, seed)
        } catch (e: IllegalArgumentException) {
            throw InitializationException(e.message)
        }

        // Load the topology
        val topology: Topology<BGPRoute> = application.loadTopology(topologyFile) {
            InterdomainTopologyReader(topologyFile).use {
                it.read()
            }
        }

        val advertisements = application.setupAdvertisements {
            // Subclasses determine how advertisements are configured, see subclasses at the bottom of this file
            initAdvertisements(topology)
        }

        val runner = RepetitionRunner(
                application,
                topology,
                advertisements,
                threshold,
                repetitions,
                messageDelayGenerator,
                metadataFile
        )

        val execution = SimpleAdvertisementExecution<BGPRoute>().apply {
            dataCollectors.add(BasicDataCollector(basicReportFile))

            if (reportNodes) {
                dataCollectors.add(NodeDataCollector(nodesReportFile))
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

        return Pair(runner, execution)
    }

    /**
     * TODO @doc
     */
    protected abstract fun initAdvertisements(topology: Topology<BGPRoute>): List<Advertisement<BGPRoute>>

    /**
     * TODO @doc
     */
    private class UsingDefaultSet(topologyFile: File, val advertiserIDs: List<NodeID>)
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
         * TODO @doc
         */
        override fun initAdvertisements(topology: Topology<BGPRoute>): List<Advertisement<BGPRoute>> {

            // Find all the advertisers from the specified IDs
            val advertisers = AdvertiserDB(topology, stubsFile, BGP(), ::parseInterdomainExtender)
                    .get(advertiserIDs)

            // In this mode, nodes set the self BGP route as the default route
            // Use the advertisements file to configure different routes
            return advertisers.map { Advertisement(it, BGPRoute.self()) }.toList()
        }
    }

    /**
     * TODO @doc
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
         * TODO @doc
         */
        override fun initAdvertisements(topology: Topology<BGPRoute>): List<Advertisement<BGPRoute>> {
            val advertiseInfo = InterdomainAdvertisementReader(advertisementsFile).use {
                it.read()
            }

            val advertiserIDs = advertiseInfo.keys.toList()
            val advertisers = AdvertiserDB(topology, stubsFile, BGP(), ::parseInterdomainExtender)
                    .get(advertiserIDs)

            return advertisers.map {
                val info = advertiseInfo[it.id] ?: AdvertisementInfo(BGPRoute.self(), 0)
                Advertisement(it, info.defaultRoute, info.time)
            }
        }
    }
}