package core.routing2

/**
 * Thrown by the topology builder when trying to add an element to the builder that was already added before.
 */
class ElementExistsException(message: String): Exception(message)


/**
 * Thrown by the topology builder when an element is required but it was not yet added to the builder.
 */
class ElementNotFoundException(message: String): Exception(message)


/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 */
class TopologyBuilder<R: Route> {

    private val nodes = HashMap<NodeID, Node<R>>()
    private val links = HashSet<Link<R>>()

    /**
     * Adds a new node with the specified ID to the builder. If a node with the same ID was already added to the
     * builder then it does not add any new node and throws an ElementExistsException.
     *
     * @param id       the ID to identify the new node
     * @param protocol the protocol deployed by the new node
     * @throws ElementExistsException if a node with the specified ID was already added to the builder
     */
    @Throws(ElementExistsException::class)
    fun addNode(id: NodeID, protocol: Protocol<R>) {

        if (nodes.putIfAbsent(id, Node(id, protocol)) != null) {
            throw ElementExistsException("Node with ID `$id` was added twice to the topology builder")
        }
    }

    /**
     * Establishes a new link connecting the node identified by the [from] ID to the node identified by the [to] ID.
     *
     * It initializes the link with the specified extender. This extender will be used to map the routes exported by
     * the [to] node and learned by the [from] node.
     *
     * @param from     the Id of the node at the tail of the link
     * @param to the protocol deployed by the new node
     * @param extender the protocol deployed by the new node
     * @throws ElementExistsException if a node with the specified ID was already added to the builder
     * @throws ElementNotFoundException if builder is missing the node with ID [from] and/or [to]
     */
    @Throws(ElementExistsException::class, ElementNotFoundException::class)
    fun link(from: NodeID, to: NodeID, extender: Extender<R>) {

        val tail = nodes[from] ?: throw ElementNotFoundException("Node with ID `$from` was not yet added the builder")
        val head = nodes[to] ?: throw ElementNotFoundException("Node with ID `$to` was not yet added the builder")

        if (!links.add(Link(tail, head, extender))) {
            throw ElementExistsException("Link from `$from` to `$to` was already added to the topology builder")
        }

        head.addInNeighbor(tail, extender)
    }

    /**
     * Returns a Topology containing the nodes and links defined in the builder at the time this method is
     * called.
     */
    fun build(): Topology<R> {
        return Topology(nodes)
    }

}