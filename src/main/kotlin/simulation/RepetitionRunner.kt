package simulation

import core.routing.Node
import core.routing.Route
import core.routing.Topology
import core.simulator.DelayGenerator
import core.simulator.Engine
import core.simulator.Time
import io.Metadata
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
        private val advertiser: Node<R>,
        private val threshold: Time,
        private val repetitions: Int,
        private val messageDelayGenerator: DelayGenerator,
        private val metadataFile: File,
        private val topologyFilename: String,
        private val stubsFilename: String?

): Runner<R> {

    /**
     * Runs the specified execution the number of times specified in the [repetitions] property.
     *
     * The engine configurations may be modified during the run. At the end of this method the engine is always
     * reverted to its defaults.
     *
     * @param execution        the execution that will be executed in each run
     */
    override fun run(execution: Execution<R>) {

        val startInstant = Instant.now()

        Engine.messageDelayGenerator = messageDelayGenerator

        application.run {

            try {
                repeat(times = repetitions) { repetition ->

                    application.execute(repetition + 1, advertiser, messageDelayGenerator.seed) {
                        execution.execute(topology, advertiser, threshold)
                    }

                    // Cleanup for next execution
                    topology.reset()
                    advertiser.reset()
                    Engine.messageDelayGenerator.generateNewSeed()
                }

            } finally {
                // Make sure that the engine is always reverted to the defaults after running
                Engine.resetToDefaults()
            }
        }

        // FIXME the metadata file cannot be written here because all info is not available
        // Output metadata
        Metadata(
                Engine.version(),
                startInstant,
                finishInstant = Instant.now(),
                topologyFilename = topologyFilename,
                stubsFilename = stubsFilename,
                destinationID = advertiser.id,
                minDelay = messageDelayGenerator.min,
                maxDelay = messageDelayGenerator.max,
                threshold = threshold
        ).print(metadataFile)

    }
}