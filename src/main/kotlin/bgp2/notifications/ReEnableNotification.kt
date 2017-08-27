package bgp2.notifications

import bgp2.BGPRoute
import core.routing2.Node
import core.simulator.notifications.Notification

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 *
 * Notification sent when a node re-enables its disabled neighbors.
 *
 * @property node               the node that detected a recurrent routing loop
 * @property reEnabledNeighbors the neighbors of 'node' that were re-enabled
 */
data class ReEnableNotification(val node: Node<BGPRoute>,
                                val reEnabledNeighbors: Collection<Node<BGPRoute>>) : Notification()