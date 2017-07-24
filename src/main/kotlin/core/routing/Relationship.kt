package core.routing

import core.simulator.Exporter

/**
 * Created on 20-07-2017
 *
 * @author David Fialho
 */
data class Relationship<N: Node, R: Route>(val node: N, val extender: Extender<N, R>, val exporter: Exporter)