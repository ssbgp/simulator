package core.routing

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 *
 * Data class representing a routing message.
 *
 * @property sender   the node that sent the message
 * @property receiver the node that will receive the messsage
 * @property route    the route to be sent
 * @property extender the extender that will be used to map the route sent by the [sender] to the route learned at
 *                    the [receiver]
 */
data class Message<R: Route>(val sender: Node<R>, val receiver: Node<R>, val route: R, val extender: Extender<R>)