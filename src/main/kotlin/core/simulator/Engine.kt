package core.simulator

import core.routing.Node
import core.routing.Topology
import core.simulator.notifications.BasicNotifier
import core.simulator.notifications.EndNotification
import core.simulator.notifications.StartNotification
import core.simulator.notifications.ThresholdReachedNotification

/**
 * Created on 23-07-2017
 *
 * @author David Fialho
 */
object Engine {

    /**
     * This variable holds the scheduler that is being used in the simulations.
     */
    var scheduler = Scheduler

    /**
     * This is the delay generator used to generate the delays for the messages.
     * By default, it uses a ZeroDelayGenerator
     * This should be changed to a different generator to obtain different behavior.
     */
    var messageDelayGenerator: DelayGenerator = ZeroDelayGenerator

    /**
     * Resets the engine to the defaults.
     */
    fun resetToDefaults() {
        scheduler = Scheduler
        messageDelayGenerator = ZeroDelayGenerator
    }

    /**
     * Runs the simulation for the given destination.
     * The threshold value determines the number of units of time the simulation should have terminated on. If this
     * threshold is reached the simulation is interrupted immediately. If no threshold is specified then the
     * simulator will run 'forever'.
     *
     * @param topology    the topology used for the simulation
     * @param destination the destination used for the simulation
     * @param threshold   a threshold value for the simulation
     * @return true if the simulation terminated before the specified threshold or false if otherwise.
     */
    fun simulate(topology: Topology<*>, destination: Node<*>, threshold: Time = Int.MAX_VALUE): Boolean {

        // Ensure the scheduler is completely clean before starting the simulation
        scheduler.reset()

        BasicNotifier.notifyStart(StartNotification(seed = 0, topology = topology))

        // The simulation execution starts when the protocol of the destination is started
        destination.start()

        var terminatedBeforeThreshold = true
        while (scheduler.hasEvents()) {
            val event = scheduler.nextEvent()

            // Check if the threshold was reached:
            // This verification needs to be performed after obtaining the next event because the scheduler's time is
            // updated when performing that action
            if (currentTime() >= threshold) {
                BasicNotifier.notifyThresholdReached(ThresholdReachedNotification(threshold))
                terminatedBeforeThreshold = false
                break
            }

            event.processIt()
        }

        BasicNotifier.notifyEnd(EndNotification())

        return terminatedBeforeThreshold
    }

}

/**
 * Cleaner way to access the simulation time.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun currentTime(): Time = Engine.scheduler.time