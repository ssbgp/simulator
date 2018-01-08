package bgp

import bgp.notifications.BGPNotifier
import bgp.notifications.DetectNotification
import core.routing.Node
import core.routing.RoutingTable
import core.simulator.Time

/**
 * Base class for the SS-BGP like protocols. Implements the deactivation of neighbors and leaves the detection
 * condition to the subclasses.
 */
abstract class BaseSSBGP(mrai: Time = 0, routingTable: RoutingTable<BGPRoute>): BaseBGP(mrai, routingTable) {

    /**
     * Invoked the the BaseBGP right after a routing loop is detected.
     *
     * An SS-BGP like protocol checks if the routing loop is recurrent and if so it deactivates the neighbor that
     * sent the route.
     */
    final override fun onLoopDetected(node: Node<BGPRoute>, sender: Node<BGPRoute>, route: BGPRoute) {

        // Ignore route learned from a disabled neighbor
        if (!routingTable.table.isEnabled(sender)) {
            return
        }

        val prevSelectedRoute = routingTable.getSelectedRoute()

        // Since a loop routing was detected, the new route via the sender node is surely invalid

        // Set the route via the sender as invalid
        // This will force the selector to select the alternative route
        val updated = routingTable.update(sender, BGPRoute.invalid())
        wasSelectedRouteUpdated = wasSelectedRouteUpdated || updated

        val alternativeRoute = routingTable.getSelectedRoute()
        if (isRecurrent(node, route, alternativeRoute, prevSelectedRoute)) {
            disableNeighbor(sender)
            BGPNotifier.notify(DetectNotification(node, route, alternativeRoute, sender))
        }
    }

    /**
     * Checks if the routing loop detected is recurrent.
     * Subclasses must implement this method to define the detection condition.
     */
    protected abstract fun isRecurrent(node: Node<BGPRoute>, learnedRoute: BGPRoute,
                                       alternativeRoute: BGPRoute, prevSelectedRoute: BGPRoute): Boolean

    /**
     * Enables the specified neighbor.
     *
     * May update the `wasSelectedRouteUpdated` property.
     *
     * @param neighbor the neighbor to enable
     */
    fun enableNeighbor(neighbor: Node<BGPRoute>) {
        val updated = routingTable.enable(neighbor)
        wasSelectedRouteUpdated = wasSelectedRouteUpdated || updated
    }

    /**
     * Disables the specified neighbor.
     *
     * May update the `wasSelectedRouteUpdated` property.
     *
     * @param neighbor the neighbor to disable
     */
    fun disableNeighbor(neighbor: Node<BGPRoute>) {
        val updated = routingTable.disable(neighbor)
        wasSelectedRouteUpdated = wasSelectedRouteUpdated || updated
    }

    override fun reset() {
        super.reset()
    }
}

/**
 * SS-BGP Protocol: when a loop is detected it tries to detect if the loop is recurrent using the WEAK detection
 * condition. If it determines the loop is recurrent, it disables the neighbor that exported the route.
 */
class SSBGP(mrai: Time = 0, routingTable: RoutingTable<BGPRoute> = RoutingTable.empty(BGPRoute.invalid()))
    : BaseSSBGP(mrai, routingTable) {

    override fun isRecurrent(node: Node<BGPRoute>, learnedRoute: BGPRoute, alternativeRoute: BGPRoute,
                             prevSelectedRoute: BGPRoute): Boolean {

        return learnedRoute.localPref > alternativeRoute.localPref
    }
}

/**
 * ISS-BGP: when a loop is detected it tries to detect if the loop is recurrent using the STRONG detection
 * condition. If it determines the loop is recurrent, it disables the neighbor that exported the route.
 */
class ISSBGP(mrai: Time = 0, routingTable: RoutingTable<BGPRoute> = RoutingTable.empty(BGPRoute.invalid()))
    : BaseSSBGP(mrai, routingTable) {

    override fun isRecurrent(node: Node<BGPRoute>, learnedRoute: BGPRoute, alternativeRoute: BGPRoute,
                             prevSelectedRoute: BGPRoute): Boolean {

        return learnedRoute.localPref > alternativeRoute.localPref &&
                alternativeRoute.asPath == learnedRoute.asPath.subPathBefore(node)
    }
}

/**
 * SS-BGP version 2 Protocol: it uses a more generic detection condition than version 1.
 */
class SSBGP2(mrai: Time = 0, routingTable: RoutingTable<BGPRoute> = RoutingTable.empty(BGPRoute.invalid()))
    : BaseSSBGP(mrai, routingTable) {

    override fun isRecurrent(node: Node<BGPRoute>, learnedRoute: BGPRoute, alternativeRoute: BGPRoute,
                             prevSelectedRoute: BGPRoute): Boolean {

        return alternativeRoute.localPref < prevSelectedRoute.localPref
    }
}

/**
 * ISS-BGP version 2 Protocol: it uses the detection condition of SS-BGP2 and also checks if the tail of looping
 * path matches the path of the alternative route.
 */
class ISSBGP2(mrai: Time = 0, routingTable: RoutingTable<BGPRoute> = RoutingTable.empty(BGPRoute.invalid()))
    : BaseSSBGP(mrai, routingTable) {

    override fun isRecurrent(node: Node<BGPRoute>, learnedRoute: BGPRoute, alternativeRoute: BGPRoute,
                             prevSelectedRoute: BGPRoute): Boolean {

        return alternativeRoute.localPref < prevSelectedRoute.localPref &&
                alternativeRoute.asPath == learnedRoute.asPath.subPathBefore(node)
    }
}
