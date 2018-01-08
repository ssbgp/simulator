package core.routing

/**
 * Base interface for any protocol implementation.
 *
 * All protocol implementations must implement this interface.
 *
 * @property selectedRoute route selected by this protocol
 *
 * Created on 19-07-17
 *
 * @author David Fialho
 */
interface Protocol<R: Route> {

    /**
     * The route selected by the protocol. This route may change during a simulation execution.
     */
    val selectedRoute: R

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