package bgp2

import bgp2.notifications.BGPNotifier
import bgp2.notifications.DetectNotification
import bgp2.notifications.ReEnableNotification
import core.routing2.Node
import core.simulator.Time
import core.simulator.Timer

/**
 * Base class for the SS-BGP like protocols. Implements the deactivation of neighbors and leaves the detection
 * condition to the subclasses.
 */
abstract class BaseSSBGP(val reenableInterval: Time, mrai: Time = 0): BaseBGP(mrai) {

    var reenableTimer = Timer.disabled()
        private set

    /**
     * Checks if the routing loop detected is recurrent.
     */
    abstract fun isRecurrent(node: Node<BGPRoute>, learnedRoute: BGPRoute, alternativeRoute: BGPRoute): Boolean

    final override fun onLoopDetected(node: Node<BGPRoute>, sender: Node<BGPRoute>, route: BGPRoute) {

        // Check if it is a routing loop if an only if the neighbor is enabled
        if (routingTable.table.isEnabled(sender)) {

            //Since a loop was detected, the route via the sender node is invalid
            var updated = routingTable.update(sender, BGPRoute.invalid())
            wasSelectedRouteUpdated = wasSelectedRouteUpdated || updated

            val alternativeRoute = routingTable.getSelectedRoute()

            if (isRecurrent(node, route, alternativeRoute)) {

                updated = routingTable.disable(sender)
                wasSelectedRouteUpdated = wasSelectedRouteUpdated || updated

                BGPNotifier.notifyDetect(DetectNotification(node, route, alternativeRoute, sender))

                if (reenableInterval > 0) {
                    reenableTimer.cancel()
                    reenableTimer = Timer.enabled(reenableInterval) { reenableNeighbors(node) }
                    reenableTimer.start()
                }
            }
        }
    }

    fun reenableNeighbors(node: Node<BGPRoute>) {

        // Make a copy of the disabled neighbors because they will be cleared when they are re-enabled
        val reEnabledNeighbors = routingTable.disabledNeighbors.toList()
        val updatedSelectedRoute = routingTable.enableAll()

        BGPNotifier.notifyReEnable(ReEnableNotification(node, reEnabledNeighbors))

        if (updatedSelectedRoute)
            export(node)
    }

    override fun reset() {
        super.reset()
        reenableTimer.cancel()
        reenableTimer = Timer.disabled()
    }
}

/**
 * SS-BGP Protocol: when a loop is detected it tries to detect if the loop is recurrent using the WEAK detection
 * condition. If it determines the loop is recurrent, it disables the neighbor that exported the route.
 */
class SSBGP(reenableInterval: Time = 0, mrai: Time = 0): BaseSSBGP(reenableInterval, mrai) {

    override fun isRecurrent(node: Node<BGPRoute>, learnedRoute: BGPRoute, alternativeRoute: BGPRoute): Boolean {
        return learnedRoute.localPref > alternativeRoute.localPref
    }
}

/**
 * ISS-BGP: when a loop is detected it tries to detect if the loop is recurrent using the STRONG detection
 * condition. If it determines the loop is recurrent, it disables the neighbor that exported the route.
 */
class ISSBGP(reenableInterval: Time = 0, mrai: Time = 0): BaseSSBGP(reenableInterval, mrai) {

    override fun isRecurrent(node: Node<BGPRoute>, learnedRoute: BGPRoute, alternativeRoute: BGPRoute): Boolean {
        return learnedRoute.localPref > alternativeRoute.localPref &&
                alternativeRoute.asPath == learnedRoute.asPath.subPathBefore(node)
    }
}