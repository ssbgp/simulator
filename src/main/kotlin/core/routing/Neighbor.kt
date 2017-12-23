package core.routing

import core.simulator.Connection

/**
 * A Neighbor is a [node] to which a local node sends messages through a [connection].
 *
 * Routes sent to a neighbor are extended by the [extender] associated with that neighbor.
 *
 * @property node       the reference to the neighboring node
 * @property extender   the extender to map routes exported from the local node to neighboring node
 * @property connection the connection with neighbor [node], used to send messages to the neighbor
 *
 * Created on 19-07-17
 *
 * @author David Fialho
 */
data class Neighbor<R: Route>(
        val node: Node<R>,
        val extender: Extender<R>,
        val connection: Connection<R> = Connection()
)