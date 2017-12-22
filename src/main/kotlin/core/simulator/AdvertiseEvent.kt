package core.simulator

import core.routing.Route

/**
 * Created on 09-11-2017
 *
 * @author David Fialho
 *
 * The advertise event triggers the [Advertiser.advertise] method of [advertiser] to have it
 * advertise [route].
 */
class AdvertiseEvent<R: Route>(private val advertiser: Advertiser<R>, private val route: R): Event {

    override fun processIt() {
        advertiser.advertise(route)
    }
}