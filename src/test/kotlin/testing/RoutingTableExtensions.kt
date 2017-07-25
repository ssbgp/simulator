package testing

import core.routing.Node
import core.routing.Route
import core.routing.RoutingTable

/**
 * Created on 24-07-2017.
 *
 * @author David Fialho
 */

/**
 * Allows us to write something like 'route(1) via node(1)'.
 * The neighbors defined here are all enabled.
 */
infix fun <N: Node, R: Route> R.via(neighbor: N): RoutingTable.Entry<N, R> {
    return RoutingTable.Entry(neighbor, this, enabled = true)
}