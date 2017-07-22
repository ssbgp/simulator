package core.simulator

import core.routing.Message


/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 */
class ExportEvent(private val message: Message) : Event {

    /**
     * Sends the message to the destination.
     */
    override fun processIt() {
        message.send()
    }

}