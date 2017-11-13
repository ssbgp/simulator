package simulation

import bgp.BGP
import bgp.BGPRoute
import core.routing.Node
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
        private val topologyFile: File,
        private val advertiserID: NodeID,
        private val reportNodes: Boolean,

        // Optional (with defaults)
        private val repetitions: Int?,
        private val minDelay: Time?,
        private val maxDelay: Time?,
        private val threshold: Time?,
        private val reportDirectory: File?,
        private val seed: Long?,

        // Optional (without defaults)
        private val stubsFile: File?

): Initializer<BGPRoute> {

    companion object {
        val DEFAULT_REPETITIONS = 1
        val DEFAULT_THRESHOLD = 1_000_000
        val DEFAULT_MINDELAY = 1
        val DEFAULT_MAXDELAY = 1
        val DEFAULT_REPORT_DIRECTORY = File(System.getProperty("user.dir"))  // current working directory
    }

    /**
     * Initializes a simulation. It sets up the executions to run and the runner to run them.
     */
    override fun initialize(application: Application, metadata: Metadata): Pair<Runner<BGPRoute>, Execution<BGPRoute>> {

        val repetitions = repetitions ?: DEFAULT_REPETITIONS
        val minDelay = minDelay ?: DEFAULT_MINDELAY
        val maxDelay = maxDelay ?: DEFAULT_MAXDELAY
        val threshold = threshold ?: DEFAULT_THRESHOLD
        val reportDirectory = reportDirectory ?: DEFAULT_REPORT_DIRECTORY
        val seed = seed ?: System.currentTimeMillis()

        // If the topology filename is `topology.nf` and the advertiserID is 10 the report filename
        // is `topology_10.basic.csv`
        val outputName = topologyFile.nameWithoutExtension
        val basicReportFile = File(reportDirectory, outputName.plus("_$advertiserID.basic.csv"))
        val nodesReportFile = File(reportDirectory, outputName.plus("_$advertiserID.nodes.csv"))
        val metadataFile = File(reportDirectory, outputName.plus("_$advertiserID.meta.txt"))

        // Setup the message delay generator
        val messageDelayGenerator = RandomDelayGenerator.with(minDelay, maxDelay, seed)

        // Load the topology
        val topology: Topology<BGPRoute> = application.loadTopology(topologyFile) {
            InterdomainTopologyReader(topologyFile).use {
                it.read()
            }
        }

        // FIXME temporary hack to avoid multiple compilation errors
        val advertiserIDs = listOf(advertiserID)

        val advertisers = application.findAdvertisers(advertiserIDs) {
            // TODO refactor, this is ugly
            // TODO replace StubDB with a better alternative

            advertiserIDs.map { id ->
                var advertiser = topology[id]

                if (stubsFile != null) {
                    advertiser = StubDB(stubsFile, BGP(), ::parseInterdomainExtender)
                            .getStub(id, topology)
                }

                advertiser ?: throw InitializationException("did not find advertiser with ID '$id'")
            }.toList()
        }

        val runner = RepetitionRunner(
                application,
                topology,
                // FIXME temporary hack to avoid multiple compilation errors
                Advertisement(advertisers[0], BGPRoute.self()),
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
        if (stubsFile != null) {
            metadata["Stubs file"] = stubsFile.name
        }
        metadata["Destination ID"] = advertiserID.toString()
        metadata["Minimum Delay"] = minDelay.toString()
        metadata["Maximum Delay"] = maxDelay.toString()
        metadata["Threshold"] = threshold.toString()

        return Pair(runner, execution)
    }
}