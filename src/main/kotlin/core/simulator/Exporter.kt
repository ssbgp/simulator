package core.simulator

import core.routing.Message

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 */
class Exporter(private val delayGenerator: DelayGenerator) {

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
    fun export(message: Message): Time {

        val delay = delayGenerator.nextDelay()
        val deliverTime = maxOf(Scheduler.time + delay, lastDeliverTime) + 1

        Scheduler.schedule(ExportEvent(message), deliverTime)
        lastDeliverTime = deliverTime

        return deliverTime
    }

}