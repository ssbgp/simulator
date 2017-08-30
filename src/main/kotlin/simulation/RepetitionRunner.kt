package simulation

import core.routing.Node
import core.routing.NodeID
import core.routing.Topology
import core.simulator.DelayGenerator
import core.simulator.Engine
import io.TopologyReaderHandler
import java.io.IOException

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 */
class RepetitionRunner(
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
     */
    override fun run(execution: Execution) {

        // Read the topology
        val topology: Topology<*> = try {
            topologyReader.read()

        } catch (e: IOException) {
            return
        }

        // Find the destination in the topology
        val destination: Node<*> = topology[destination] ?: return

        Engine.messageDelayGenerator = messageDelayGenerator

        try {
            repeat(times = repetitions) {
                execution.execute(topology, destination)

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