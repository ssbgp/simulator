package core.simulator

import core.routing.Message

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 *
 * ExporterWithoutDelay is an implementation of an exporter that does not introduce an delay to the exported messages.
 * Each messages takes exactly one unit of time to reach teh destination.
 */
class ExporterWithoutDelay : Exporter {

    override fun export(message: Message) {
        Scheduler.scheduleFromNow(ExportEvent(message), interval = 1)
    }

}