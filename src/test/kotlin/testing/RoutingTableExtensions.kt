package testing

import core.routing.Node
import core.routing.Route
import core.routing.RoutingTable

/**
 * Allows us to write something like 'route(1) via node(1)'.
 * The neighbors defined here are all enabled.
 */
infix fun <R: Route> R.via(neighbor: Node<R>): RoutingTable.Entry<R> {
    return RoutingTable.Entry(neighbor, this, enabled = true)
}