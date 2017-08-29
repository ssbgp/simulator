package core.routing

/**
 * Alias used for node IDs
 * This makes it easy to change the ID type to long if an Int is too small
 */
typealias NodeID = Int

/**
 * Created on 19-07-17
 *
 * @author David Fialho
 *
 * A node is the fundamental component of a topology @see Topology. A node is some entity that is able to speak with
 * other nodes using a common protocol.
 *
 * @property id The ID of the node. This ID uniquely identifies it inside a topology
 */
class Node<R: Route>(val id: NodeID, val protocol: Protocol<R>) {

    private val mutableInNeighbors = ArrayList<Neighbor<R>>()

    /**
     * List containing the in-neighbors of this node.
     */
    val inNeighbors: List<Neighbor<R>>
        get() = mutableInNeighbors

    /**
     * Sets a new in-neighbor for this node.
     *
     * @param neighbor the in-neighbor node
     * @param extender the extender used to map routes from this node to the in-neighbor
     */
    fun addInNeighbor(neighbor: Node<R>, extender: Extender<R>) {
        mutableInNeighbors.add(Neighbor(neighbor, extender))
    }

    /**
     * Starts the protocol deployed by this node.
     */
    fun start() {
        protocol.start(this)
    }

    /**
     * Sends a message containing the route [route] to all in-neighbors of this node.
     *
     * @param route the route to be sent
     */
    fun send(route: R) {

        for ((neighbor, extender, exporter) in inNeighbors) {
            val message = Message(this, neighbor, route, extender)
            exporter.export(message)
        }
    }

    /**
     * Receives a message from an out-neighbor of this node.
     * This method should be invoked when a new message is expected to arrive to this node and be processed by it.
     */
    fun receive(message: Message<R>) {
        protocol.process(message)
    }

    /**
     * Two nodes are considered equal if they have exactly the same ID.
     * Subclasses of node should not override this method.
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
    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return "Node($id)"
    }

}