package core.routing2

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 *
 * A Path is a sequence of nodes that form a path in a network. Nodes appended to a path are kept in order.
 * Nodes can be repeated in a path. That is, a path may include two or more nodes with the same ID.
 *
 * Path instances are immutable!
 *
 * @property size expresses the number of nodes in the path
 */
class Path internal constructor(private val nodes: List<Node<*>>) : Iterable<Node<*>> {

    val size: Int = nodes.size

    /**
     * Returns a new path instance containing the nodes in the same order as this path and with the given node
     * appended to it.
     */
    fun append(node: Node<*>): Path {
        val nodesCopy = ArrayList(nodes)
        nodesCopy.add(node)

        return Path(nodesCopy)
    }

    /**
     * Returns the next-hop node of the path.
     */
    fun nextHop(): Node<*>? {
        return nodes.lastOrNull()
    }

    /**
     * Checks if this path contains the given node.
     */
    operator fun contains(node: Node<*>) = node in nodes

    /**
     * Returns a path instance containing exactly the same nodes as this path and exactly in the same order.
     */
    fun copy(): Path = Path(nodes)

    /**
     * Returns a path corresponding to the sub-path from the beginning of the path until the fir node equal to the
     * specified node.
     */
    fun subPathBefore(node: Node<*>): Path {

        val nodeIndex = nodes.indexOf(node)
        return if (nodeIndex >= 0) Path(nodes.subList(0, nodeIndex)) else this
    }

    /**
     * Returns an iterator over the nodes of the path. The iterator starts at first node in the path.
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
fun emptyPath(): Path {
    return Path(emptyList())
}

/**
 * Returns a path containing the given nodes in the same order as they are given.
 */
fun pathOf(vararg nodes: Node<*>): Path {
    return Path(listOf(*nodes))
}

/**
 * Returns an empty path.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun pathOf(): Path {
    return emptyPath()
}