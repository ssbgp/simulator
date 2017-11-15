package simulation

import bgp.BGP
import bgp.BGPRoute
import core.routing.NodeID
import core.routing.Topology
import core.simulator.Advertisement
import core.simulator.RandomDelayGenerator
import core.simulator.Time
import io.InterdomainTopologyReader
import io.parseInterdomainExtender
import ui.Application
import java.io.File

/**
 * Created on 09-11-2017
 *
 * @author David Fialho
 */
class BGPAdvertisementInitializer(
        // Mandatory
        val topologyFile: File,
        val advertiserIDs: List<NodeID>,    // must include at least one ID

        // Optional (with defaults)
        var repetitions: Int = DEFAULT_REPETITIONS,
        minDelay: Time = DEFAULT_MINDELAY,
        maxDelay: Time = DEFAULT_MAXDELAY,
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
    }

    var minDelay: Time = minDelay
        set(value) {
            if (value > maxDelay) {
                throw InitializationException("minimum delay must be lower than or equal to maximum delay")
            }

            field = value
        }

    var maxDelay: Time = maxDelay
        set(value) {
            if (value < minDelay) {
                throw InitializationException("maximum delay must be higher than or equal to minimum delay")
            }

            field = value
        }

    init {
        // Verify that at least 1 advertiser ID is provided in the constructor
        if (advertiserIDs.isEmpty()) {
            throw IllegalArgumentException("initializer requires at least 1 advertiser ID")
        }
    }

    /**
     * Initializes a simulation. It sets up the executions to run and the runner to run them.
     */
    override fun initialize(application: Application, metadata: Metadata): Pair<Runner<BGPRoute>, Execution<BGPRoute>> {

        // If no seed is set, then a new seed is generated, based on the current time, for each new initialization
        val seed = seed ?: System.currentTimeMillis()

        // The output name (excluding the extension) corresponds to the topology filename and
        // the IDs of the advertisers. For instance, if the topology file name is `topology.nf` and
        // the advertiser IDs are 10 and 12, then the output file name will be
        // `topology_10-12`
        val outputName = topologyFile.nameWithoutExtension + "_${advertiserIDs.joinToString("-")}"

        // Append extensions according to the file type
        val basicReportFile = File(reportDirectory, outputName.plus(".basic.csv"))
        val nodesReportFile = File(reportDirectory, outputName.plus(".nodes.csv"))
        val metadataFile = File(reportDirectory, outputName.plus(".meta.txt"))

        // Setup the message delay generator
        val messageDelayGenerator = RandomDelayGenerator.with(minDelay, maxDelay, seed)

        // Load the topology
        val topology: Topology<BGPRoute> = application.loadTopology(topologyFile) {
            InterdomainTopologyReader(topologyFile).use {
                it.read()
            }
        }

        // Find all the advertisers from the specified IDs
        val advertisers = application.findAdvertisers(advertiserIDs) {
            AdvertiserDB(topology, stubsFile, BGP(), ::parseInterdomainExtender)
                    .get(advertiserIDs)
        }

        // Create advertisements for each advertiser
        // TODO @feature - replace use of BGP's self route with a route defined by the user
        // Here we are using the BGP self route as the advertised/default route to have the same
        // behavior as before
        val advertisements = advertisers.map { Advertisement(it, BGPRoute.self()) }.toList()

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
        metadata["Advertiser(s)"] = advertiserIDs.joinToString()
        metadata["Minimum Delay"] = minDelay.toString()
        metadata["Maximum Delay"] = maxDelay.toString()
        metadata["Threshold"] = threshold.toString()

        return Pair(runner, execution)
    }
}