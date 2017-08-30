package core.routing


/**
 * Data class to represent an uni-directional link in a topology.
 *
 * @property tail     the node at the tail of the link
 * @property head     the node at the head of the link
 * @property extender the extender used to map routes exported by the head node to the tail node
 */
data class Link<R: Route>(val tail: Node<R>, val head: Node<R>, val extender: Extender<R>)