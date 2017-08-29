package core.routing

/**
 * This is the basic interface for a protocol implementation.
 */
interface Protocol<R: Route> {

    /**
     * Starts this protocol.
     */
    fun start(node: Node<R>)

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