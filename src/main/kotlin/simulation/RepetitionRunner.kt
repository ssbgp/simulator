package simulation

import core.routing.Node
import core.routing.NodeID
import core.routing.Topology
import core.simulator.DelayGenerator
import core.simulator.Engine
import io.TopologyReaderHandler
import ui.Application
import java.io.File

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 */
class RepetitionRunner(
        private val topologyFile: File,
        private val topologyReader: TopologyReaderHandler,
        private val destination: NodeID,
        private val repetitions: Int,
        private val messageDelayGenerator: DelayGenerator

): Runner {

    /**
     * Runs the specified execution the number of times specified in the [repetitions] property.
     *
     * The engine configurations may be modified during the run. At the end of this method the engine is always
     * reverted to its defaults.
     *
     * @param execution        the execution that will be executed in each run
     * @param application the application running that wants to monitor progress and handle errors
     */
    override fun run(execution: Execution, application: Application) {

        val topology: Topology<*> = application.loadTopology(topologyFile, topologyReader) {
            topologyReader.read()
        }

        val destination: Node<*> = application.findDestination(destination) {
            topology[destination]
        }

        Engine.messageDelayGenerator = messageDelayGenerator

        application.run {

            try {
                repeat(times = repetitions) { repetition ->

                    application.execute(repetition, destination, messageDelayGenerator.seed) {
                        execution.execute(topology, destination)
                    }

                    // Cleanup for next execution
                    topology.reset()
                    Engine.messageDelayGenerator.generateNewSeed()
                }

            } finally {
                // Make sure that the engine is always reverted to the defaults after running
                Engine.resetToDefaults()
            }
        }
    }
}