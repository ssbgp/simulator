package bgp2.notifications

import bgp2.BGPRoute
import core.routing2.Message
import core.simulator.notifications.Notification

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 *
 * Notification sent when a node sends a message.
 *
 * @property message the message sent
 */
data class MessageSentNotification(val message: Message<BGPRoute>) : Notification()
