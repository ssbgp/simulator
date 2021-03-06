package core.simulator

import core.routing.Message
import core.routing.Route
import core.simulator.notifications.MessageSentNotification
import core.simulator.notifications.Notifier

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 *
 * This class abstracts a connection through which messages can flow. A connection can carry
 * messages in a single direction.
 *
 * The [Connection] class provides a [send] method, which abstracts the process of sending a
 * message across the connection. A message sent through a connection is subjected to a random
 * delay obtained from the delay generator [Simulator.messageDelayGenerator]. Despite the delays
 * generated by this generator, routing messages are always delivered in first-in-first-out order.
 *
 */
class Connection<R: Route> {

    /**
     * Keeps track of the deliver time of the last message sent through this connection.
     */
    private var lastDeliverTime = 0

    /**
     * Sends a [message] through this connection. It subjects the message to a random delay
     * obtained from the delay generator [Simulator.messageDelayGenerator].
     *
     * !! It adds an event to the scheduler [Simulator.scheduler] !!
     */
    fun send(message: Message<R>): Time {

        val delay = Simulator.messageDelayGenerator.nextDelay()
        val deliverTime = maxOf(Simulator.scheduler.time + delay, lastDeliverTime) + 1

        Simulator.scheduler.schedule(MessageEvent(message), deliverTime)
        lastDeliverTime = deliverTime

        Notifier.notify(MessageSentNotification(message))
        return deliverTime
    }

    /**
     * Resets the connection.
     *
     * After being reset, the connection will not remember previous messages it may have sent.
     * Consequently, new messages sent through this connection may be delivered before the
     * messages sent before calling [reset]. To avoid unexpected behavior, call this method only
     * after ensuring all sent messages have been delivered.
     */
    fun reset() {
        lastDeliverTime = 0
    }

}