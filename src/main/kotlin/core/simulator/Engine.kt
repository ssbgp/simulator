package core.simulator

import core.routing.Destination

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
     * Runs the simulation for the given destination.
     */
    fun simulate(destination: Destination) {

        destination.announceItSelf()

        while (scheduler.hasEvents()) {
            scheduler.nextEvent().processIt()
        }

    }

}