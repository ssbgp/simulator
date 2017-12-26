package core.routing

import core.simulator.Advertiser

/**
 * Alias used for node IDs
 * This makes it easy to change the ID type to long if an Int is too small
 */
typealias NodeID = Int

/**
 * A Node is the fundamental element of a topology.
 *
 * A node can represent any entity that is able to speak with other nodes through a common
 * routing protocol. The protocol deployed at each node does not need be exactly the same. The
 * only requirement is that the routes exchanged are of the same type.
 *
 * Each node has unique ID. This ID is only unique for nodes within the same topology.
 *
 * @property id       the ID of the node, which uniquely identifies it inside a topology
 * @property protocol the protocol deployed by this node
 *
 * Created on 19-07-17
 *
 * @author David Fialho
 */
class Node<R : Route>(override val id: NodeID, val protocol: Protocol<R>) : Advertiser<R> {

    /**
     * Collection containing the in-neighbors of this node.
     */
    val mutableInNeighbors = ArrayList<Neighbor<R>>()
    val inNeighbors: Collection<Neighbor<R>>
        get() = mutableInNeighbors

    /**
     * Adds a new in-neighbor to this node.
     *
     * @param neighbor the in-neighbor node to add
     * @param extender the extender used to map routes from this node to the in-neighbor
     */
    fun addInNeighbor(neighbor: Node<R>, extender: Extender<R>) {
        mutableInNeighbors.add(Neighbor(neighbor, extender))
    }

    /**
     * Have this node set [defaultRoute] as its default route and advertise it to in-neighbors
     * according to its deployed protocol specifications.
     */
    override fun advertise(defaultRoute: R) {
        protocol.setLocalRoute(this, defaultRoute)
    }

    /**
     * Have this node send a message containing the given [route] to all of its in-neighbors.
     */
    fun export(route: R) {
        inNeighbors.forEach { send(route, it) }
    }

    /**
     * Have this node send a messages containing the given [route] to a [neighbor].
     */
    private fun send(route: R, neighbor: Neighbor<R>) {
        val message = Message(this, neighbor.node, neighbor.extender.extend(route, this))

        neighbor.connection.send(message)
    }

    /**
     * Have this node receive a [message] from an out-neighbor. The [message] is passed through
     * the routing protocol deployed by this node and processed.
     *
     * The simulator should invoke this method when it wants to have a message arrive at some node.
     */
    fun receive(message: Message<R>) {
        protocol.process(message)
    }

    /**
     * Resets this node's state.
     */
    override fun reset() {
        protocol.reset()
        inNeighbors.forEach { it.connection.reset() }
    }

    /**
     * Two nodes are considered equal if they have exactly the same ID.
     *
     * Subclasses of node should NOT override this method.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node<*>

        if (id != other.id) return false

        return true
    }

    /**
     * Follows the equals/hashCode contract.
     */
    override fun hashCode(): Int = id

    override fun toString(): String = "Node($id)"
}