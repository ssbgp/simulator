package core.simulator

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
class AdvertisementStrategy : Iterable<Advertisement> {

    /**
     * Stores are advertisements planned in this strategy.
     */
    private val advertisements = ArrayList<Advertisement>()

    /**
     * Plans an advertisement.
     */
    fun plan(advertisement: Advertisement) {
        advertisements.add(advertisement)
    }

    /**
     * Returns an iterator over the planned advertisements
     */
    override fun iterator(): Iterator<Advertisement> = advertisements.iterator()

    /**
     * Checks whether or not the strategy includes any planned advertisements.
     */
    fun isEmpty(): Boolean = advertisements.isEmpty()

}