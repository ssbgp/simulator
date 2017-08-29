package core.simulator.notifications

import core.routing.Message

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 *
 * Notification sent when a node sends a message.
 *
 * @property message the message sent
 */
data class MessageSentNotification(val message: Message<*>) : Notification()