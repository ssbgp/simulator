package core.simulator

import core.routing.Message

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 */
class ExporterWithDelay(private val delayGenerator: DelayGenerator) : Exporter {

    /**
     * Stores the timestamp of the time at which the last message exported using this exporter was delivered to its
     * destination.
     */
    private var lastDeliverTime = 0

    override fun export(message: Message) {

        val delay = delayGenerator.nextDelay()
        val deliverTime = maxOf(Scheduler.time + delay, lastDeliverTime) + 1

        Scheduler.schedule(ExportEvent(message), deliverTime)
        lastDeliverTime = deliverTime
    }

}