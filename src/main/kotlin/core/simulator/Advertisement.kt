package core.simulator

import core.routing.Route

/**
 * Created on 08-11-2017
 *
 * @author David Fialho
 *
 * An [Advertisement] instance contains all information to describe an advertisement: the
 * [advertiser] performing it; the [route] being advertised; and the [time] at which the
 * advertisement occurs.
 */
data class Advertisement<R: Route>(
        val advertiser: Advertiser<R>,
        val route: R,
        val time: Time = 0
)