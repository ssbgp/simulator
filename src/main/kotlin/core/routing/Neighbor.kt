package core.routing

import core.simulator.Connection

/**
 * Created on 19-07-17
 *
 * @author David Fialho
 *
 * Data class containing all the attributes that define a neighbor.
 *
 * @property node       the reference to the neighboring node
 * @property extender   the extender to map routes exported from the local node to neighboring node
 * @property connection the connection with neighbor [node], used to send messages to the neighbor
 */
data class Neighbor<R: Route>(
        val node: Node<R>,
        val extender: Extender<R>,
        val connection: Connection<R> = Connection()
)