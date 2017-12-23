package core.routing

/**
 * A Path is a sequence of nodes that form a path in a network.
 *
 * Nodes appended to a path are kept in the same order as they are added.
 *
 * Path instances are immutable! All operations that would modify a path (@see [Path.append]) do
 * not actually modify that path instance. Instead, they generate a new instance with the
 * corresponding modification and return that instance.
 *
 * The same node can be added multiple times to a path. If a routing protocol does not allow
 * that. Then, the protocol is responsible for ensuring that does not happen.
 *
 * Notice that although the Path class includes nodes it does not require the type of route to be
 * specified. That is intentional. The path class only stores nodes, and it only cares about their
 * order. It does not perform any operations that require knowing the type of route.
 *
 * @property size the number of nodes in the path
 *
 * Created on 21-07-2017
 *
 * @author David Fialho
 */
class Path internal constructor(private val nodes: List<Node<*>>) : Iterable<Node<*>> {

    val size: Int = nodes.size

    /**
     * Returns a new path instance with [node] added to the end of (appended to) this path.
     */
    fun append(node: Node<*>): Path {
        val nodesCopy = ArrayList(nodes)
        nodesCopy.add(node)

        return Path(nodesCopy)
    }

    /**
     * Returns the next-hop node of the path. That is the node at the end of this path. If the
     * path is empty it returns null.
     */
    fun nextHop(): Node<*>? {
        return nodes.lastOrNull()
    }

    /**
     * Checks if this path contains [node].
     */
    operator fun contains(node: Node<*>) = node in nodes

    /**
     * Returns a shallow copy of this path. In other words, returns a path instance containing
     * exactly the same nodes in the exact same order.
     */
    fun copy(): Path = Path(nodes)  // This works as a copy only because paths are immutable

    /**
     * Returns a path corresponding to the sub-path from the beginning of the path until the first
     * node equal to [node].
     */
    fun subPathBefore(node: Node<*>): Path {
        val nodeIndex = nodes.indexOf(node)
        return if (nodeIndex >= 0) Path(nodes.subList(0, nodeIndex)) else this
    }

    /**
     * Returns an iterator over the nodes of the path. The iterator goes through the path
     * starting at the first node.
     */
    override fun iterator(): Iterator<Node<*>> {
        return nodes.iterator()
    }

    /**
     * Two paths are considered equal if they have the exact same nodes in the exact same order.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Path

        if (nodes != other.nodes) return false

        return true
    }

    override fun hashCode(): Int {
        return nodes.hashCode()
    }

    override fun toString(): String {
        return "$nodes"
    }

}

/**
 * Returns a path with no nodes.
 */
fun emptyPath(): Path = Path(emptyList())

/**
 * Returns a path containing the given nodes in the same order as they are given.
 */
fun pathOf(vararg nodes: Node<*>): Path = Path(listOf(*nodes))

/**
 * Returns an empty path.
 */
fun pathOf(): Path = emptyPath()