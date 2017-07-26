package bgp

import bgp.notifications.*
import core.simulator.Time
import core.simulator.Timer

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 */
sealed class BaseBGPProtocol(private val mrai: Time) {

    var mraiTimer = Timer.disabled()
        private set

    /**
     * Flag that indicates if a new route was selected as a result of processing a new incoming message. This flag is
     * always set to false when a new message arrives and should only be set to true if a new route is selected when
     * the message is being processed.
     */
    var wasSelectedRouteUpdated: Boolean = false
        protected set

    /**
     * Processes a BGP message received by a node.
     * May updated the routing table and the selected route/neighbor.
     *
     * @param message the message to be processed
     */
    fun process(message: BGPMessage) {

        val node = message.receiver

        // Store the route the node was selecting before processing this message
        val previousSelectedRoute = node.routingTable.getSelectedRoute()

        val importedRoute = import(message.sender, message.route, message.extender)
        BGPNotifier.notifyImport(ImportNotification(node, importedRoute, message.sender))

        val learnedRoute = learn(node, message.sender, importedRoute)
        BGPNotifier.notifyLearn(LearnNotification(node, learnedRoute, message.sender))

        val updated = node.routingTable.update(message.sender, learnedRoute)

        // Set updated flag to true if 'updated' is true or keep its current state
        wasSelectedRouteUpdated = wasSelectedRouteUpdated || updated

        if (wasSelectedRouteUpdated) {

            val selectedRoute = node.routingTable.getSelectedRoute()
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
    fun import(sender: BGPNode, route: BGPRoute, extender: BGPExtender): BGPRoute {
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
    fun learn(node: BGPNode, sender: BGPNode, route: BGPRoute): BGPRoute {

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
    fun export(node: BGPNode) {

        if (mraiTimer.expired) {

            val exportedRoute = node.routingTable.getSelectedRoute()
            node.export(exportedRoute)

            BGPNotifier.notifyExport(ExportNotification(node, exportedRoute))

            if (mrai > 0) {
                mraiTimer = Timer.enabled(mrai) {
                    // this will only be executed when the timer expires!
                    export(node)
                }
                mraiTimer.start()
            }

        }

    }

    /**
     * Resets the state of the protocol as if it was just created.
     */
    fun reset() {
        mraiTimer = Timer.disabled()
        wasSelectedRouteUpdated = false
    }

    /**
     * Called by the protocol when it detects a routing loop.
     */
    abstract fun onLoopDetected(node: BGPNode, sender: BGPNode, route: BGPRoute)

}

//region Subclasses

/**
 * BGP Protocol: when a loop is detected it does nothing
 */
class BGPProtocol(mrai: Time = 0) : BaseBGPProtocol(mrai) {
    override fun onLoopDetected(node: BGPNode, sender: BGPNode, route: BGPRoute) = Unit
}

/**
 * Base class for the SS-BGP like protocols. Implements the deactivation of neighbors and leaves the detection
 * condition to the subclasses.
 */
abstract class BaseSSBGPProtocol(val reenableInterval: Time, mrai: Time = 0) : BaseBGPProtocol(mrai) {

    var reenableTimer = Timer.disabled()
        private set

    /**
     * Checks if the routing loop detected is recurrent.
     */
    abstract fun isRecurrentRoutingLoop(node: BGPNode, learnedRoute: BGPRoute, alternativeRoute: BGPRoute): Boolean

    final override fun onLoopDetected(node: BGPNode, sender: BGPNode, route: BGPRoute) {

        //Since a loop was detected, the route via the sender node is invalid
        var updated = node.routingTable.update(sender, BGPRoute.invalid())
        wasSelectedRouteUpdated = wasSelectedRouteUpdated || updated

        val alternativeRoute = node.routingTable.getSelectedRoute()

        if (isRecurrentRoutingLoop(node, route, alternativeRoute)) {
            updated = node.routingTable.disable(sender)
            wasSelectedRouteUpdated = wasSelectedRouteUpdated || updated

            BGPNotifier.notifyDetect(DetectNotification(node, route, alternativeRoute, sender))

            if (reenableInterval > 0) {
                reenableTimer.cancel()
                reenableTimer = Timer.enabled(reenableInterval) { reenableNeighbors(node) }
                reenableTimer.start()
            }
        }
    }

    fun reenableNeighbors(node: BGPNode) {
        val updatedSelectedRoute = node.routingTable.enableAll()

        if (updatedSelectedRoute)
            export(node)
    }
}

/**
 * SS-BGP Protocol: when a loop is detected it tries to detect if the loop is recurrent using the WEAK detection
 * condition. If it determines the loop is recurrent, it disables the neighbor that exported the route.
 */
class SSBGPProtocol(reenableInterval: Time = 0, mrai: Time = 0) : BaseSSBGPProtocol(reenableInterval, mrai) {

    override fun isRecurrentRoutingLoop(node: BGPNode, learnedRoute: BGPRoute, alternativeRoute: BGPRoute): Boolean {
        return learnedRoute.localPref > alternativeRoute.localPref
    }
}

/**
 * SS-BGP Protocol: when a loop is detected it tries to detect if the loop is recurrent using the STRONG detection
 * condition. If it determines the loop is recurrent, it disables the neighbor that exported the route.
 */
class ISSBGPProtocol(reenableInterval: Time = 0, mrai: Time = 0) : BaseSSBGPProtocol(reenableInterval, mrai) {

    override fun isRecurrentRoutingLoop(node: BGPNode, learnedRoute: BGPRoute, alternativeRoute: BGPRoute): Boolean {
        return learnedRoute.localPref > alternativeRoute.localPref &&
                alternativeRoute.asPath == learnedRoute.asPath.subPathBefore(node)
    }
}

//endregion