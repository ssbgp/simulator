package core.simulator

import core.routing.Route
import core.routing.Topology
import core.simulator.notifications.BasicNotifier
import core.simulator.notifications.EndNotification
import core.simulator.notifications.StartNotification
import core.simulator.notifications.ThresholdReachedNotification
import java.util.*

/**
 * Created on 23-07-2017
 *
 * @author David Fialho
 */
object Engine {

    /**
     * This variable holds the scheduler that is being used in the simulations.
     */
    var scheduler = Scheduler()

    /**
     * This is the delay generator used to generate the delays for the messages.
     * By default, it uses a NoDelayGenerator
     * This should be changed to a different generator to obtain different behavior.
     */
    var messageDelayGenerator: DelayGenerator = NoDelayGenerator

    /**
     * Resets the engine to the defaults.
     */
    fun resetToDefaults() {
        scheduler = Scheduler()
        messageDelayGenerator = NoDelayGenerator
    }

    /**
     * Runs a simulation with a single advertisement.
     *
     * The scheduler is cleared before running the simulation.
     *
     * The threshold value determines the number of units of time the simulation should have terminated on. If this
     * threshold is reached the simulation is interrupted immediately. If no threshold is specified then the
     * simulator will run 'forever'.
     *
     * @param topology      the topology used for the simulation
     * @param advertisement the single advertisement to start off the simulation
     * @param threshold     a threshold value for the simulation
     * @return true if the simulation terminated before the specified threshold or false if otherwise.
     */
    fun <R: Route> simulate(topology: Topology<R>, advertisement: Advertisement<R>,
                            threshold: Time = Int.MAX_VALUE): Boolean {
        return simulate(topology, listOf(advertisement), threshold)
    }

    /**
     * Runs a simulation with one or multiple advertisements.
     *
     * The scheduler is cleared before running the simulation.
     *
     * The threshold value determines the number of units of time the simulation should have terminated on. If this
     * threshold is reached the simulation is interrupted immediately. If no threshold is specified then the
     * simulator will run 'forever'.
     *
     * @param topology       the topology used for the simulation
     * @param advertisements a list containing all the advertisements to occur in the simulation
     * @param threshold      a threshold value for the simulation
     * @return true if the simulation terminated before the specified threshold or false if otherwise
     * @throws IllegalArgumentException if the advertisement plan is empty
     */
    @Throws(IllegalArgumentException::class)
    fun <R: Route> simulate(topology: Topology<*>, advertisements: List<Advertisement<R>>,
                            threshold: Time = Int.MAX_VALUE): Boolean {

        if (advertisements.isEmpty()) {
            throw IllegalArgumentException("a simulation requires at least one advertisement")
        }

        // Ensure the scheduler is completely clean before starting the simulation
        scheduler.reset()

        BasicNotifier.notifyStart(StartNotification(messageDelayGenerator.seed, topology))

        // Schedule advertisements specified in the strategy
        for (advertisement in advertisements) {
            scheduler.schedule(advertisement)
        }

        // Flag that will indicate whether or not the simulation finished before the threshold was reached
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

        // Notify listeners the simulation ended
        BasicNotifier.notifyEnd(EndNotification(topology))

        return terminatedBeforeThreshold
    }

    /**
     * Returns the simulator version.
     * It obtains the version from the resource where the simulator's version is defined.
     */
    fun version(): String {

        javaClass.getResourceAsStream("/version.properties").use {
            val properties = Properties()
            properties.load(it)
            return properties.getProperty("application.version")
        }
    }

}


/**
 * Schedules an advertisement event.
 */
private fun <R: Route> Scheduler.schedule(advertisement: Advertisement<R>) {
    schedule(AdvertiseEvent(advertisement.advertiser, advertisement.route), advertisement.time)
}

/**
 * Cleaner way to access the simulation time.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun currentTime(): Time = Engine.scheduler.time