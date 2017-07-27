package testing.bgp

import bgp.BGPNode
import bgp.BGPRoute
import core.routing.Path

/**
 * Created on 26-07-2017.
 *
 * @author David Fialho
 *
 * This file contains helper functions for tests with BGP.
 */

/**
 * Returns a path of BGPNodes with the specified IDs and in the specified order.
 */
fun pathOf(vararg ids: Int): Path<BGPNode> {
    val nodes = ids.map { BGPNode.with(it) }.toTypedArray()
    return core.routing.pathOf(*nodes)
}

/**
 * Shorter way to create a valid BGP route.
 */
fun route(localPref: Int, asPath: Path<BGPNode>) = BGPRoute.with(localPref, asPath)

/**
 * Shorter way to create an invalid BGP route.
 */
fun invalid() = BGPRoute.invalid()

