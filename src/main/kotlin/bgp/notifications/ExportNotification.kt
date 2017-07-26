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
 * Notification sent when a node exports a route received to its in-neighbors.
 * This notification is sent to indicate that a node exported a route. It does not specify to which neighbors the
 * route was exported and it is sent only once. To be notified of each routing message that is sent use the
 * MessageSentNotification.
 *
 * @property node  the node that exported a route
 * @property route the route that was exported
 */
data class ExportNotification(override val time: Time, val node: BGPNode, val route: BGPRoute) : Notification