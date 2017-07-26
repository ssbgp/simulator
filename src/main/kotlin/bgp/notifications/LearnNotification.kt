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
 * Notification sent when a node learns a route from one of its out-neighbors.
 *
 * @property node     the node that learned a route
 * @property route    the route that was learned
 * @property neighbor the neighbor from which the route was learned
 */
data class LearnNotification
(val node: BGPNode, val route: BGPRoute, val neighbor: BGPNode) : Notification()