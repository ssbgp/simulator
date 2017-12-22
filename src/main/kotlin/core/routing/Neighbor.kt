package core.routing

import core.simulator.Exporter

/**
 * Created on 19-07-17
 *
 * @author David Fialho
 *
 * Data class containing all the attributes that define a neighbor.
 *
 * All nodes have a set of neighbors. A neighbor is another node in the topology.
 *
 * @property node     the neighbor node
 * @property extender the extender to map routes exported from the local node to neighbor [node]
 * @property exporter the exporter to export routes from the local node to neighbor [node]
 */
data class Neighbor<R: Route>(
        val node: Node<R>,
        val extender: Extender<R>,
        val exporter: Exporter<R> = Exporter()
)