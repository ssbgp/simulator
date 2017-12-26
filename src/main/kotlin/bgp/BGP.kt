package bgp

import bgp.notifications.*
import core.routing.*
import core.simulator.Time
import core.simulator.Timer

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 */
abstract class BaseBGP(val mrai: Time, routingTable: RoutingTable<BGPRoute>) : Protocol<BGPRoute> {

    /**
     * Routing table containing the candidate routes.
     * Uses a route selector to perform the route selection.
     * The route selector wraps the provided routing table if one is provided. Otherwise, it wraps a new routing table.
     */
    val routingTable = RouteSelector.wrap(routingTable, ::bgpRouteCompare)

    /**
     * The route selected by the protocol.
     */
    override val selectedRoute: BGPRoute
        get() = routingTable.getSelectedRoute()

    var mraiTimer = Timer.disabled()
        protected set

    /**
     * Flag that indicates if a new route was selected as a result of processing a new incoming message. This flag is
     * always set to false when a new message arrives and should only be set to true if a new route is selected when
     * the message is being processed.
     */
    protected var wasSelectedRouteUpdated: Boolean = false

    /**
     * Stores the last route that was exported to neighbors.
     * This is used to ensure that a selected and exported is not exported again when the MRAI expires.
     */
    private var lastExportedRoute = BGPRoute.invalid()

    /**
     * Sets [route] as the the local route of [node]. The local route is handled as any other
     * candidate route learned from a neighbor. Therefore, this action may lead to a newly
     * exported route.
     *
     * @param node  the node to set local route for
     * @param route the route to set as the local route
     */
    override fun setLocalRoute(node: Node<BGPRoute>, route: BGPRoute) {
        process(node, node, route)
    }

    /**
     * Processes a BGP message received by a node.
     * May update the routing table and the selected route/neighbor.
     *
     * @param message the message to be processed
     */
    override fun process(message: Message<BGPRoute>) {
        process(message.recipient, message.sender, message.route)
    }

    /**
     * Processes a BGP route imported by a node.
     * May update the routing table and the selected route/neighbor.
     *
     * @param node          the node that imported the route
     * @param neighbor      the neighbor that exported the route
     * @param importedRoute the route imported by [node]
     */
    fun process(node: Node<BGPRoute>, neighbor: Node<BGPRoute>, importedRoute: BGPRoute) {

        // Store the route the node was selecting before processing this message
        val previousSelectedRoute = routingTable.getSelectedRoute()

        val learnedRoute = learn(node, neighbor, importedRoute)
        BGPNotifier.notify(LearnNotification(node, learnedRoute, neighbor))

        val updated = routingTable.update(neighbor, learnedRoute)

        // Set updated flag to true if 'updated' is true or keep its current state
        wasSelectedRouteUpdated = wasSelectedRouteUpdated || updated

        if (wasSelectedRouteUpdated) {

            val selectedRoute = routingTable.getSelectedRoute()
            BGPNotifier.notify(
                    SelectNotification(node, selectedRoute, previousSelectedRoute))

            export(node)
            wasSelectedRouteUpdated = false
        }
    }

    /**
     * Implements the process of learning a route.
     *
     * @param node   the node processing the route
     * @param sender the out-neighbor that sent the route
     * @param route  the route imported by the node (route obtained after applying the extender)
     * @return the imported route if the route's AS-PATH does not include the node learning the route or it returns
     * an invalid if the route's AS-PATH includes the learning node. Note that it may also return an invalid route if
     * the imported route is invalid.
     */
    protected fun learn(node: Node<BGPRoute>, sender: Node<BGPRoute>, route: BGPRoute): BGPRoute {

        if (node in route.asPath) {
            // Notify the implementations that a loop was detected
            onLoopDetected(node, sender, route)

            return BGPRoute.invalid()
        } else {
            return route
        }
    }

    /**
     * Implements the process of exporting a route. It exports the currently selected by the node.
     *
     * @param node  the node exporting the node
     */
    protected fun export(node: Node<BGPRoute>) {

        if (mraiTimer.isRunning) {
            // No route is exported while the MRAI timer is running
            return
        }

        val selectedRoute = routingTable.getSelectedRoute()

        if (selectedRoute == lastExportedRoute) {
            // Do not export the same route consecutively
            return
        }

        // Export the route currently selected
        node.export(selectedRoute)
        BGPNotifier.notify(ExportNotification(node, selectedRoute))
        lastExportedRoute = selectedRoute

        if (mrai > 0) {
            // Restart the MRAI timer
            mraiTimer = Timer.enabled(mrai) {
                export(node)    // when the timer expires
            }
        }
    }

    /**
     * Resets the state of the protocol as if it was just created.
     */
    override fun reset() {
        routingTable.clear()
        wasSelectedRouteUpdated = false
        mraiTimer.cancel()
        mraiTimer = Timer.disabled()
        lastExportedRoute = BGPRoute.invalid()
    }

    /**
     * Called by the protocol when it detects a routing loop.
     */
    abstract protected fun onLoopDetected(node: Node<BGPRoute>, sender: Node<BGPRoute>, route: BGPRoute)

}

/**
 * BGP: when a loop is detected it does nothing extra.
 */
class BGP(mrai: Time = 0, routingTable: RoutingTable<BGPRoute> = RoutingTable.empty(BGPRoute.invalid()))
    : BaseBGP(mrai, routingTable) {

    override fun onLoopDetected(node: Node<BGPRoute>, sender: Node<BGPRoute>, route: BGPRoute) = Unit
}
