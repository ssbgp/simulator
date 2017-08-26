package core.simulator2

import core.routing2.Message
import core.routing2.Route
import core.simulator.Time

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 */
class Exporter<R: Route> {

    /**
     * Stores the timestamp of the time at which the last message exported using this exporter was delivered to its
     * destination.
     */
    private var lastDeliverTime = 0

    /**
     * It issues an export event with the specified message.
     *
     * @return the deliver time of the exported message
     */
    fun export(message: Message<R>): Time {

        val delay = Engine.messageDelayGenerator.nextDelay()
        val deliverTime = maxOf(Engine.scheduler.time + delay, lastDeliverTime) + 1

        Engine.scheduler.schedule(ExportEvent(message), deliverTime)
        lastDeliverTime = deliverTime

        return deliverTime
    }

}