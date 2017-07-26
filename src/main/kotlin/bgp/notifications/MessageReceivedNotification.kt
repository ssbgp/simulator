package bgp.notifications

import bgp.BGPMessage
import core.simulator.Time
import core.simulator.notifications.Notification


/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 *
 * Notification sent when a node receives a new message.
 *
 * @property message the message received
 */
data class MessageReceivedNotification
(override val time: Time, val message: BGPMessage) : Notification