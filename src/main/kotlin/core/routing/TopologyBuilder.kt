package core.routing

/**
 * Thrown to indicate that an element being added already exists and can not be re-added.
 */
class ElementExistsException(message: String) : Exception(message)

/**
 * Thrown to indicate that an element was not found and was expected to exist.
 */
class ElementNotFoundException(message: String) : Exception(message)

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 *
 * A builder for a topology. The [Topology] class is immutable. This builder must used to build
 * topologies.
 *
 * This class is implemented following the builder pattern. It provides methods to build a topology
 * incrementally and a [build] method to create the topology instance based on the builder's
 * current state.
 */
class TopologyBuilder<R : Route> {

    private val nodes = HashMap<NodeID, Node<R>>()
    private val links = HashSet<Link<R>>()

    /**
     * Adds a new node with the given [id] and deploying the given [protocol]. It throws an
     * [ElementExistsException] if a node with the given [id] has been already added.
     *
     * @return this builder
     */
    @Throws(ElementExistsException::class)
    fun addNode(id: NodeID, protocol: Protocol<R>): TopologyBuilder<R> {

        if (nodes.putIfAbsent(id, Node(id, protocol)) != null) {
            throw ElementExistsException("node with ID `$id` was already added to builder")
        }

        return this
    }

    /**
     * Has the builder establish a link associated with [extender] from node identified by ID [from]
     * to node identified by ID [to]. This extender will be used to map routes exported by the
     * [to] node and learned at the [from] node.
     *
     * @return this builder
     * @throws ElementNotFoundException if nodes with IDs [from] and/or [to] were not added to
     * the builder yet
     * @throws ElementExistsException if a link already exists between nodes [from] and [to]
     */
    @Throws(ElementExistsException::class, ElementNotFoundException::class)
    fun link(from: NodeID, to: NodeID, extender: Extender<R>): TopologyBuilder<R> {

        val tail = nodes[from] ?: throw ElementNotFoundException("node with ID `$from` was not to builder yet")
        val head = nodes[to] ?: throw ElementNotFoundException("node with ID `$to` was not to builder yet")

        if (!links.add(Link(tail, head, extender))) {
            throw ElementExistsException("nodes $from and $to are already linked")
        }

        head.addInNeighbor(tail, extender)

        return this
    }

    /**
     * Returns a new Topology containing the nodes and links defined in the builder at the time
     * this method is called.
     */
    fun build(): Topology<R> {
        return Topology(nodes)
    }

}