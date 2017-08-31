package io

import core.routing.Route
import core.routing.Topology

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 */
interface TopologyReader<R: Route> {

    fun read(): Topology<R>
}