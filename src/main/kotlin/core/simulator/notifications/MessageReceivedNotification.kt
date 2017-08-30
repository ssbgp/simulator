package core.simulator.notifications

import core.routing.Message

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 *
 * Notification sent when a node receives a message.
 *
 * @property message the message received
 */
data class MessageReceivedNotification(val message: Message<*>) : Notification()