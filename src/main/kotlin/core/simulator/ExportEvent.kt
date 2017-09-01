package core.simulator

import core.routing.Route
import core.routing.Message


/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 */
class ExportEvent<R: Route>(private val message: Message<R>) : Event {

    /**
     * Sends the message to the receiver node.
     */
    override fun processIt() {
        message.receiver.receive(message)
    }

}