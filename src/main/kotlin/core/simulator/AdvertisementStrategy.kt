package core.simulator

import core.routing.Route

/**
 * Created on 08-11-2017
 *
 * @author David Fialho
 *
 * The advertisement strategy describes when should advertisements occur.
 * It specifies the instants of time when each advertiser should advertise a destination.
 *
 * Advertisement strategies are used by the Engine to run simulations with one or more advertisements that advertise the
 * destination at different instants of time.
 */
class AdvertisementStrategy<R: Route> : Iterable<Advertisement<R>> {

    /**
     * Stores are advertisements planned in this strategy.
     */
    private val advertisements = ArrayList<Advertisement<R>>()

    /**
     * Plans an advertisement.
     */
    fun plan(advertisement: Advertisement<R>) {
        advertisements.add(advertisement)
    }

    /**
     * Returns an iterator over the planned advertisements
     */
    override fun iterator(): Iterator<Advertisement<R>> = advertisements.iterator()

    /**
     * Checks whether or not the strategy includes any planned advertisements.
     */
    fun isEmpty(): Boolean = advertisements.isEmpty()

}