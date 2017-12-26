package core.routing

/**
 * A Topology is an high-level abstraction of a network composed of nodes and their
 * interconnections.
 *
 * The topology class is immutable. That is, nodes and links can not be added or removed from the
 * topology. A topology must be built using a topology builder, @see [TopologyBuilder].
 *
 * Each node in a topology is uniquely identified by its ID. The topology provides access to its
 * nodes from an ID through its [get] operator.
 *
 * @property size      the number of nodes in the topology
 * @property linkCount the number of links in the topology
 * @property nodes     collection containing all nodes in the topology in no particular order
 * @property links     collection containing all links in the topology in no particular order
 *
 * Created on 16-07-2017.
 *
 * @author David Fialho
 */
class Topology<R : Route>(private val idToNode: Map<NodeID, Node<R>>) {

    /**
     * Number of nodes in the topology.
     */
    val size: Int = idToNode.size

    /**
     * Number of links in the topology.
     */
    val linkCount: Int
        get() = idToNode.map { it.value.inNeighbors }.count()

    /**
     * Collection containing all nodes in the topology in no particular order.
     */
    val nodes: Collection<Node<R>> = idToNode.values

    /**
     * Collection containing all links in the topology in no particular order.
     */
    val links: Collection<Link<R>>
        get() {

            val links = ArrayList<Link<R>>()

            for (node in nodes) {
                for ((neighbor, extender, _) in node.inNeighbors) {
                    links.add(Link(neighbor, node, extender))
                }
            }

            return links
        }

    /**
     * Returns the node with [id] or null if this topology does not contain any node with [id].
     */
    operator fun get(id: Int): Node<R>? = idToNode[id]

    /**
     * Resets the topology state. It resets the state of all nodes in the topology.
     */
    fun reset() {
        nodes.forEach { it.reset() }
    }

}

/**
 * Data class to represent an uni-directional link in a topology.
 *
 * @property tail     the node at the tail of the link
 * @property head     the node at the head of the link
 * @property extender the extender used to map routes exported by the head node to the tail node
 */
data class Link<R : Route>(val tail: Node<R>, val head: Node<R>, val extender: Extender<R>)