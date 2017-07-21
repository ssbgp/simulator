package core.routing

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 *
 * Represents a message sent from one node to another.
 */
interface Message {

    /**
     * Sends the message to the receiver. The receiver to which the message is sent is dependent on the implementation.
     */
    fun send()
}