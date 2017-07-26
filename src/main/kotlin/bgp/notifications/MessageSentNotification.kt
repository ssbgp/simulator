package bgp.notifications

import bgp.BGPMessage
import core.simulator.Time
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
data class MessageSentNotification
(override val time: Time, val message: BGPMessage) : Notification
