package testing2

import core.routing2.Node
import core.routing2.Route
import core.routing2.RoutingTable

/**
 * Allows us to write something like 'route(1) via node(1)'.
 * The neighbors defined here are all enabled.
 */
infix fun <R: Route> R.via(neighbor: Node<R>): RoutingTable.Entry<R> {
    return RoutingTable.Entry(neighbor, this, enabled = true)
}