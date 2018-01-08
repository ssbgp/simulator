package core.simulator

import core.routing.Message
import core.routing.Route
import core.simulator.notifications.MessageReceivedNotification
import core.simulator.notifications.Notifier

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 *
 * A [MessageEvent] is issued when a message is sent and it occurs (it is processed) when the
 * message is delivered to its recipient node.
 */
class MessageEvent<R : Route>(private val message: Message<R>) : Event {

    /**
     * Sends [message] to its recipient node.
     */
    override fun processIt() {
        Notifier.notify(MessageReceivedNotification(message))
        message.recipient.receive(message)
    }

}