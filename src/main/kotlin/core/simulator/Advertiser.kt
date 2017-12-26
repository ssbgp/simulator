package core.simulator

import core.routing.NodeID
import core.routing.Route

/**
 * Created on 08-11-2017
 *
 * @author David Fialho
 *
 * An [Advertiser] is some entity that can advertise destinations. The term *advertise* here is
 * used to refer to an entity originating and new route for some destination and propagating to
 * its neighbors.
 *
 * @property id number that uniquely identifies the advertiser within some scope.
 */
interface Advertiser<in R: Route> {

    val id: NodeID

    /**
     * Has this advertiser originate [defaultRoute] for some destination and propagate this route
     * through the network using any of the mechanisms implemented by this advertiser.
     */
    fun advertise(defaultRoute: R)

    // TODO @refactor - remove this method, see RepetitionRunner
    /**
     * Resets the state of the advertiser. This may be required before advertising.
     */
    fun reset()
}