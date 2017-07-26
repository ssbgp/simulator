package testing.mock

import bgp.BGPNode
import bgp.BGPRoute
import bgp.bgpRouteCompare
import core.routing.NodeID
import core.routing.RouteSelector
import core.routing.RoutingTable
import org.mockito.Mockito

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 */

/**
 * Work around over Mockito.any() to avoid error: 'IllegalStateException: Mockito.any() must not be null'
 */
fun <T> any(): T {
    Mockito.any<T>()
    @Suppress("UNCHECKED_CAST")
    return null as T
}


/**
 * Returns a mocked node with the specified ID and routing table. If no routing table is provided it uses an empty
 * routing table.
 */
fun mockNodeWith(id: NodeID, table: RoutingTable<BGPNode, BGPRoute> = RoutingTable.empty(BGPRoute.invalid())): BGPNode {

    val node = Mockito.mock(BGPNode::class.java)
    Mockito.`when`(node.id).thenReturn(id)

    val selector = Mockito.spy(RouteSelector.wrap(table, ::bgpRouteCompare))
    Mockito.`when`(node.routingTable).thenReturn(selector)

    return node
}

/**
 * Resets the routing table of a mocked node.
 */
fun resetRoutingTable(node: BGPNode, table: RoutingTable<BGPNode, BGPRoute> = RoutingTable.empty(BGPRoute.invalid())) {

    val selector = Mockito.spy(RouteSelector.wrap(table, ::bgpRouteCompare))
    Mockito.`when`(node.routingTable).thenReturn(selector)
}