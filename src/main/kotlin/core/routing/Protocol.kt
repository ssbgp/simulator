package core.routing

/**
 * Base interface for any protocol implementation.
 *
 * All protocol implementations must implement this interface.
 *
 * Routing protocols may require knowledge about in-neighbors. Depending on the protocol
 * implementation, this neighbors may need to be stored/organized in different ways for the
 * protocol to run more efficiently. To allow that level of customization, the protocol itself
 * stores the in-neighbors, not the node deploying the protocol.
 *
 * @property inNeighbors collection of all in-neighbors of the node deploying this protocol
 * @property selectedRoute route selected by this protocol
 *
 * Created on 19-07-17
 *
 * @author David Fialho
 */
interface Protocol<R: Route> {

    /**
     * Collection of all the in-neighbors added to the protocol.
     */
    val inNeighbors: Collection<Neighbor<R>>

    /**
     * The route selected by the protocol. This route may change during a simulation execution.
     */
    val selectedRoute: R

    /**
     * Adds a new in-neighbor for the protocol to take into account.
     */
    fun addInNeighbor(neighbor: Neighbor<R>)

    /**
     * Sets the local [route] for [node].
     */
    fun setLocalRoute(node: Node<R>, route: R)

    /**
     * Have the protocol process and incoming [message].
     *
     * The receiving node calls this method when it receives a message.
     */
    fun process(message: Message<R>)

    /**
     * Resets the state of the protocol to its initial state.
     */
    fun reset()

}