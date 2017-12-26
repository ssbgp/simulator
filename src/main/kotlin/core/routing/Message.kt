package core.routing

/**
 * Data class containing all information contained in a routing message.
 *
 * Nodes participating in a distributed routing protocol exchange routes, containing routing
 * information, with neighboring nodes to provide connectivity to each other. Routes are carried
 * by messages.
 *
 * As with any message, a routing message holds the [sender] and [recipient] of the message. Most
 * importantly, a message carries a [route], sent by the [sender] to the [recipient].
 *
 * @property sender    the node that sent the message
 * @property recipient the node to receive the message
 * @property route     the route sent by the sender
 *
 * Created on 21-07-2017
 *
 * @author David Fialho
 */
data class Message<R : Route>(
        val sender: Node<R>,
        val recipient: Node<R>,
        val route: R
)