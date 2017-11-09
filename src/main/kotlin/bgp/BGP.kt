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
abstract class BaseBGP(val mrai: Time, routingTable: RoutingTable<BGPRoute>): Protocol<BGPRoute> {

    /**
     * This data structure contains all neighbors that the protocol needs to send routes to when a new
     * route is selected.
     */
    protected val neighbors = ArrayList<Neighbor<BGPRoute>>()

    /**
     * Collection of all the in-neighbors added to the protocol.
     */
    override val inNeighbors: Collection<Neighbor<BGPRoute>>
        get() = neighbors

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
     * Adds a new in-neighbor for the protocol to export selected routes to.
     *
     * It does not check if the neighbor was already added to the protocol. Thus, the same neighbor can be added
     * twice, which means that it will be notified twice every time a new route is selected.
     */
    override fun addInNeighbor(neighbor: Neighbor<BGPRoute>) {
        neighbors.add(neighbor)
    }

    /**
     * Makes [node] advertise a destination and sets [defaultRoute] as the default route to reach that destination.
     * The default route is immediately exported if it becomes the selected route.
     *
     * @param node         the node to advertise destination
     * @param defaultRoute the default route to reach the destination
     */
    override fun advertise(node: Node<BGPRoute>, defaultRoute: BGPRoute) {
        process(node, node, defaultRoute)
    }

    override fun advertise(node: Node<BGPRoute>) {
        process(node, node, BGPRoute.self())
    }

    /**
     * Processes a BGP message received by a node.
     * May update the routing table and the selected route/neighbor.
     *
     * @param message the message to be processed
     */
    override fun process(message: Message<BGPRoute>) {

        val importedRoute = import(message.sender, message.route, message.extender)
        BGPNotifier.notifyImport(ImportNotification(message.receiver, importedRoute, message.sender))

        process(message.receiver, message.sender, importedRoute)
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
        BGPNotifier.notifyLearn(LearnNotification(node, learnedRoute, neighbor))

        val updated = routingTable.update(neighbor, learnedRoute)

        // Set updated flag to true if 'updated' is true or keep its current state
        wasSelectedRouteUpdated = wasSelectedRouteUpdated || updated

        if (wasSelectedRouteUpdated) {

            val selectedRoute = routingTable.getSelectedRoute()
            BGPNotifier.notifySelect(
                    SelectNotification(node, selectedRoute, previousSelectedRoute))

            export(node)
            wasSelectedRouteUpdated = false
        }
    }

    /**
     * Implements the process of importing a route.
     * Returns the result of extending the given route with the given extender.
     *
     * @param sender   the node the sent the route
     * @param route    the route received by the node (route obtained directly from the message)
     * @param extender the extender used to import the route (extender included in the message)
     */
    protected fun import(sender: Node<BGPRoute>, route: BGPRoute, extender: Extender<BGPRoute>): BGPRoute {
        return extender.extend(route, sender)
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

        if (!mraiTimer.expired) {
            // The MRAI timer is still running: no route is exported while the MRAI timer is running
            return
        }

        val selectedRoute = routingTable.getSelectedRoute()

        if (selectedRoute == lastExportedRoute) {
            // Do not export if the selected route is equal to the last route exported by the node
            return
        }

        // Export the route currently selected
        node.send(selectedRoute)
        BGPNotifier.notifyExport(ExportNotification(node, selectedRoute))
        lastExportedRoute = selectedRoute

        if (mrai > 0) {
            // Restart the MRAI timer
            mraiTimer = Timer.enabled(mrai) {
                export(node)    // when the timer expires
            }

            mraiTimer.start()
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
