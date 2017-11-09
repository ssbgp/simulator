package core.simulator

import core.routing.Route

/**
 * Created on 08-11-2017
 *
 * @author David Fialho
 *
 * An advertisement is a data class that specifies the advertiser and the time at which it will/did take place.
 */
data class Advertisement<R: Route>(val advertiser: Advertiser<R>, val route: R, val time: Time = 0)