package core.routing

/**
 * This is the basic interface for a protocol implementation.
 */
interface Protocol<R: Route> {

    /**
     * Collection of all the in-neighbors added to the protocol.
     */
    val inNeighbors: Collection<Neighbor<R>>

    /**
     * The route selected by the protocol.
     */
    val selectedRoute: R

    /**
     * Adds a new in-neighbor for the protocol to consider.
     */
    fun addInNeighbor(neighbor: Neighbor<R>)

    /**
     * Have [node] advertise a destination.
     */
    fun advertise(node: Node<R>)

    /**
     * Processes an incoming routing message.
     *
     * This method is invoked by the node using this protocol when it receives a new routing message that must be
     * processed.
     */
    fun process(message: Message<R>)

    /**
     * Resets the state of the protocol to its initial state.
     */
    fun reset()

}