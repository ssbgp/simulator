package simulation

import core.routing.Route
import core.routing.Topology
import core.simulator.Advertisement
import core.simulator.DelayGenerator
import core.simulator.Simulator
import core.simulator.Time
import io.KeyValueWriter
import ui.Application
import java.io.File
import java.time.Instant

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 */
class RepetitionRunner<R: Route>(
        private val application: Application,
        private val topology: Topology<R>,
        private val advertisements: List<Advertisement<R>>,
        private val threshold: Time,
        private val repetitions: Int,
        private val messageDelayGenerator: DelayGenerator,
        private val metadataFile: File?

): Runner<R> {

    /**
     * Runs the specified execution the number of times specified in the [repetitions] property.
     *
     * The simulator's configurations may be modified during the run. At the end of this method the
     * simulator is always reverted to its defaults.
     *
     * @param execution the execution that will be executed in each run
     * @param metadata  a metadata instance that may already contain some meta values
     */
    override fun run(execution: Execution<R>, metadata: Metadata) {

        val startInstant = Instant.now()
        Simulator.messageDelayGenerator = messageDelayGenerator

        application.run {

            try {
                repeat(times = repetitions) { repetition ->

                    application.execute(repetition + 1, advertisements, messageDelayGenerator.seed) {
                        execution.execute(topology, advertisements, threshold)
                    }

                    // Cleanup for next execution
                    topology.reset()
                    // TODO @refactor - put stubs in the topology itself to avoid having this
                    //                  reset() method in the advertiser interface
                    advertisements.forEach { it.advertiser.reset() }
                    Simulator.messageDelayGenerator.generateNewSeed()
                }

            } finally {
                // Make sure that the simulator is always reverted to the defaults after running
                Simulator.resetToDefaults()
            }
        }

        metadata["Start Time"] = startInstant
        metadata["Finish Time"] = Instant.now()

        if (metadataFile != null) {
            application.writeMetadata(metadataFile) {

                KeyValueWriter(metadataFile).use {
                    for ((key, value) in metadata) {
                        it.write(key, value)
                    }
                }
            }
        }

    }
}