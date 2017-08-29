package bgp.notifications

import bgp.BGPRoute
import core.routing.Node
import core.simulator.Notification

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
data class ExportNotification(val node: Node<BGPRoute>, val route: BGPRoute) : Notification()