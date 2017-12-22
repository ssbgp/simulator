package bgp.notifications

import bgp.BGPRoute
import core.routing.Node
import core.simulator.notifications.Notification

/**
 * Notification issued when a [node] has a [route] to send to its in-neighbors.
 *
 * Exporting a route is different from sending a message. A route being exported indicates the
 * [node] has a new [route] to send to its in-neighbors. This may lead to zero or multiple
 * messages being sent: during one exportation, the node sends one message to each in-neighbor.
 *
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
data class ExportNotification(
        val node: Node<BGPRoute>,
        val route: BGPRoute
) : Notification()

/**
 * Notification issued when a [node] learns a [route] from a [neighbor].
 *
 * The route a node receives corresponds exactly to the route selected at the sender. However, in
 * a real network that is not the case. In a real network the route learned at a node from a
 * neighbor is given by the export policies of the sender and the import policies of the receiver.
 * The simulator uses an extending function to model that transformation, @see
 * [core.routing.Extender]. Thus, the learned [route] corresponds to the result of applying the
 * extender associated with the link from [neighbor] to [node] to route received at [node]. One
 * exception to this, is when the resulting route already include [node] in its path. In that
 * case, the learned route is the invalid route.
 *
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
data class LearnNotification(
        val node: Node<BGPRoute>,
        val route: BGPRoute,
        val neighbor: Node<BGPRoute>
) : Notification()

/**
 * Notification issued when a [node] detects a recurrent routing loop after processing a route
 * from a [neighbor].
 *
 * This notification only applies to SS-BGP protocols.
 *
 * @property learnedRoute  the route learned from [neighbor]
 * @property selectedRoute the route selected by [node] as a result o learning the [learnedRoute]
 * from [neighbor]
 *
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
data class DetectNotification(
        val node: Node<BGPRoute>,
        val learnedRoute: BGPRoute,
        val selectedRoute: BGPRoute,
        val neighbor: Node<BGPRoute>
) : Notification()

/**
 * Notification issued when a [node] selects a new route [selectedRoute] over route [previousRoute].
 *
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
data class SelectNotification(
        val node: Node<BGPRoute>,
        val selectedRoute: BGPRoute,
        val previousRoute: BGPRoute
) : Notification()


