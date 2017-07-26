package bgp.notifications

import bgp.BGPNode
import bgp.BGPRoute
import core.simulator.Time
import core.simulator.notifications.Notification

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 *
 * Notification sent when a node selects a new route.
 *
 * @property node          the node that selected a new route
 * @property selectedRoute the newly route selected
 * @property previousRoute the route being selected before
 */
data class SelectNotification
(val node: BGPNode, val selectedRoute: BGPRoute, val previousRoute: BGPRoute) : Notification()