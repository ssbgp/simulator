package bgp

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
        private set

    /**
     * Processes a BGP message received by a node.
     * May updated the routing table and the selected route/neighbor.
     *
     * @param message the message to be processed
     */
    fun process(message: BGPMessage) {

        val node = message.receiver
        val importedRoute = import(message.sender, message.route, message.extender)
        val learnedRoute = learn(node, message.sender, importedRoute)

        val updated = node.routingTable.update(message.sender, learnedRoute)

        // Set updated flag to true if 'updated' is true or keep its current state
        wasSelectedRouteUpdated = wasSelectedRouteUpdated || updated

        if (wasSelectedRouteUpdated) {
            export(node)
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
            onLoopDetected(sender, route)

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
            node.export(node.routingTable.getSelectedRoute())

            if (mrai > 0) {
                mraiTimer = Timer.enabled(mrai) {
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
    protected abstract fun onLoopDetected(sender: BGPNode, route: BGPRoute)

}

//region Subclasses

/**
 * BGP Protocol: when a loop is detected it does nothing
 */
class BGPProtocol(mrai: Time = 0) : BaseBGPProtocol(mrai) {
    override fun onLoopDetected(sender: BGPNode, route: BGPRoute) = Unit
}

/**
 * SS-BGP Protocol: when a loop is detected it tries to detect if the loop is recurrent using the WEAK detection
 * condition. If it determines the loop is recurrent, it disables the neighbor that exported the route.
 */
class SSBGPProtocol(mrai: Time = 0) : BaseBGPProtocol(mrai) {

    override fun onLoopDetected(sender: BGPNode, route: BGPRoute) {
        TODO("not implemented")
    }
}

/**
 * SS-BGP Protocol: when a loop is detected it tries to detect if the loop is recurrent using the STRONG detection
 * condition. If it determines the loop is recurrent, it disables the neighbor that exported the route.
 */
class ISSBGPProtocol(mrai: Time = 0) : BaseBGPProtocol(mrai) {
    override fun onLoopDetected(sender: BGPNode, route: BGPRoute) {
        TODO("not implemented")
    }
}

//endregion