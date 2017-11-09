package core.simulator

import core.routing.Route

/**
 * Created on 09-11-2017
 *
 * @author David Fialho
 */
class AdvertiseEvent<R: Route>(private val advertiser: Advertiser<R>, private val route: R): Event {

    /**
     * Processes this event.
     */
    override fun processIt() {
        advertiser.advertise(route)
    }
}