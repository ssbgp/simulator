package core.simulator

import core.routing.NodeID
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
     * Each advertiser is associated with a unique ID.
     */
    val id: NodeID

    /**
     * Advertises some destination.
     *
     * @param defaultRoute the default route for the destination.
     */
    fun advertise(defaultRoute: R)

    /**
     * Resets the state of the advertiser. This may be required before advertising.
     */
    fun reset()
}