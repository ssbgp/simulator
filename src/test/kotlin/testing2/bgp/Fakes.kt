package testing2.bgp

import bgp2.BGP
import bgp2.BGPRoute
import core.routing2.Path
import core.routing2.Node
import core.routing2.NodeID
import core.routing2.pathOf

/**
 * Created on 26-07-2017.
 *
 * @author David Fialho
 *
 * This file contains helper functions for tests with BGP.
 */

/**
 * Creates a node with the specified ID and deploying the BGP protocol.
 */
fun BGPNode(id: NodeID): Node<BGPRoute> {
    return Node(id, protocol = BGP())
}

/**
 * Shorter way to create a valid BGP route.
 */
fun route(localPref: Int, asPath: Path) = BGPRoute.with(localPref, asPath)

/**
 * Shorter way to create an invalid BGP route.
 */
fun invalid() = BGPRoute.invalid()

/**
 * Returns a path of nodes deploying the BGP protocol with the specified IDs and in the specified order.
 */
fun pathOf(vararg ids: Int): Path {
    val nodes = ids.map { BGPNode(it) }.toTypedArray()
    return pathOf(*nodes)
}
