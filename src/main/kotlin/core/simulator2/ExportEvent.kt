package core.simulator2

import core.routing2.Route
import core.routing2.Message
import core.simulator.Event


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