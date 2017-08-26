package core.routing2

/**
 * This is the basic interface for a protocol implementation.
 */
interface Protocol<R: Route> {

    /**
     * Starts this protocol.
     */
    fun start()

    /**
     * Processes an incoming routing message.
     *
     * This method is invoked by the node using this protocol when it receives a new routing message that must be
     * processed.
     */
    fun processIt(message: Message<R>)

}