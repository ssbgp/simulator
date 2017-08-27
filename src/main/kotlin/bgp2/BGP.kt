package bgp2

import bgp2.notifications.*

import core.routing2.*
import core.simulator.Time
import core.simulator.Timer

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 */
abstract class BaseBGP(private val mrai: Time): Protocol<BGPRoute> {

    /**
     * Routing table containing the candidate routes.
     */
    val routingTable = RouteSelector.wrapNewTable(BGPRoute.invalid(), ::bgpRouteCompare)

    var mraiTimer = Timer.disabled()
        private set

    /**
     * Flag that indicates if a new route was selected as a result of processing a new incoming message. This flag is
     * always set to false when a new message arrives and should only be set to true if a new route is selected when
     * the message is being processed.
     */
    var wasSelectedRouteUpdated: Boolean = false
        protected set

    private var lastExportedRoute: BGPRoute = BGPRoute.invalid()

    override fun start() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Processes a BGP message received by a node.
     * May updated the routing table and the selected route/neighbor.
     *
     * @param message the message to be processed
     */
    override fun process(message: Message<BGPRoute>) {

        val node = message.receiver

        // Store the route the node was selecting before processing this message
        val previousSelectedRoute = routingTable.getSelectedRoute()

        val importedRoute = import(message.sender, message.route, message.extender)
        BGPNotifier.notifyImport(ImportNotification(node, importedRoute, message.sender))

        val learnedRoute = learn(node, message.sender, importedRoute)
        BGPNotifier.notifyLearn(LearnNotification(node, learnedRoute, message.sender))

        val updated = routingTable.update(message.sender, learnedRoute)

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
    fun import(sender: Node<BGPRoute>, route: BGPRoute, extender: Extender<BGPRoute>): BGPRoute {
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
    fun learn(node: Node<BGPRoute>, sender: Node<BGPRoute>, route: BGPRoute): BGPRoute {

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
    fun export(node: Node<BGPRoute>) {

        val selectedRoute = routingTable.getSelectedRoute()

        if (!mraiTimer.expired) {
            // The MRAI timer is still running: no route is exported while the MRAI timer is running
            return
        }

        if (lastExportedRoute == selectedRoute) {
            // There is no new route to export
            return
        }

        // Export the route currently selected
        node.send(selectedRoute)
        BGPNotifier.notifyExport(ExportNotification(node, selectedRoute))

        // Update the last exported route
        lastExportedRoute = selectedRoute

        // Check if the MRAI feature is enabled: it is enabled only if the MRAI is greater than 0
        if (mrai > 0) {

            // Restart the MRAI timer
            mraiTimer = Timer.enabled(mrai) { export(node) } // when the timer expires
            mraiTimer.start()
        }

    }

    /**
     * Resets the state of the protocol as if it was just created.
     */
    open fun reset() {
        mraiTimer.cancel()
        mraiTimer = Timer.disabled()
        wasSelectedRouteUpdated = false
    }

    /**
     * Called by the protocol when it detects a routing loop.
     */
    abstract fun onLoopDetected(node: Node<BGPRoute>, sender: Node<BGPRoute>, route: BGPRoute)

}

/**
 * BGP: when a loop is detected it does nothing extra.
 */
class BGP(mrai: Time = 0) : BaseBGP(mrai) {
    override fun onLoopDetected(node: Node<BGPRoute>, sender: Node<BGPRoute>, route: BGPRoute) = Unit
}
