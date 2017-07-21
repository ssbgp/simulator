package bgp

import core.routing.Path
import core.routing.Route
import core.routing.emptyPath

/**
 * Created on 20-07-2017
 *
 * @author David Fialho
 *
 * A BGP route is composed of two attributes: the LOCAL-PREF and the AS-PATH. The LOCAL-PREF is assigned locally by
 * each node and indicates the degree of preference that node assigns to each route. The AS-PATH contains the
 * sequence of nodes traversed by the route from the original advertiser to the current node holding the route.
 *
 * BGP routes are always immutable instances!
 */
interface BGPRoute : Route {

    val localPref: Int
    val asPath: Path<BGPNode>

}

/**
 * An implementation for a valid BGP route.
 */
internal class ValidBGPRoute(override val localPref: Int, override val asPath: Path<BGPNode>) : BGPRoute {

    // A valid bgp route is always valid
    override fun isValid(): Boolean = true

}

/**
 * An implementation for a invalid BGP route.
 */
object InvalidBGPRoute : BGPRoute {

    override val localPref: Int = Int.MIN_VALUE
    override val asPath: Path<BGPNode> = emptyPath()

    // A invalid bgp route is always invalid
    override fun isValid(): Boolean = false

}

//region Factory functions

/**
 * Returns a valid BGP route with the given LOCAL-PREF and AS-PATH.
 */
fun BGPRouteWith(localPref: Int, asPath: Path<BGPNode>): BGPRoute {
    return ValidBGPRoute(localPref, asPath)
}

/**
 * Returns an invalid BGP route
 */
fun invalidBGPRoute(): BGPRoute {
    return InvalidBGPRoute
}

//endregion
