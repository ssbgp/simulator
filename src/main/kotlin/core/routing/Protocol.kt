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
     * Makes [node] advertise a destination and sets [defaultRoute] as the default route to reach that destination.
     *
     * @param node         the node to advertise destination
     * @param defaultRoute the default route to reach the destination
     */
    fun advertise(node: Node<R>, defaultRoute: R)

    // TODO remove this. kept here to avoid compilation errors during the transition
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