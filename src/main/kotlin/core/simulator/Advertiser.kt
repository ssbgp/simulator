package core.simulator

import core.routing.Route

/**
 * Created on 08-11-2017
 *
 * @author David Fialho
 *
 * An advertiser is some entity that can advertise destinations.
 */
interface Advertiser<in R: Route> {

    /**
     * Advertises some destination.
     *
     * @param defaultRoute the default route for the destination.
     */
    fun advertise(defaultRoute: R)

    // TODO remove this. kept here to avoid compilation errors during the transition
    fun advertise()
}