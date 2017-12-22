package core.routing

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 *
 * Data class representing a routing message.
 *
 * In a distributed routing protocol, nodes exchange routing messages to share routing information
 * with their neighbors. A routing message
 *
 * @property sender   the node that sent the message
 * @property receiver the node to receive the message
 * @property route    the route sent by the sender
 * @property extender the extender used to map [route] to the learned route at the [receiver]
 */
data class Message<R: Route>(
        val sender: Node<R>,
        val receiver: Node<R>,
        val route: R,
        val extender: Extender<R>
)