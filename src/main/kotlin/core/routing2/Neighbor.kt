package core.routing2

import core.simulator.Exporter

/**
 * Data class containing all the attributes that define a neighbor.
 *
 * @property node     the neighbor node
 * @property extender the extender used to map routes exported to this neighbor
 * @property exporter the exporter used to export routes to this neighbor
 */
data class Neighbor<R: Route>(val node: Node<R>, val extender: Extender<R>, val exporter: Exporter)