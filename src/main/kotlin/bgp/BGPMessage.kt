package bgp

import core.routing.Message

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 */
data class BGPMessage
(val sender: BGPNode, val receiver: BGPNode, val route: BGPRoute, val extender: BGPExtender) : Message {

    /**
     * Sends the message to the receiver BGP node.
     */
    override fun send() {
        receiver.onReceivingMessage(this)
    }

}