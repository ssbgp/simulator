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
sealed class BGPRoute : Route {

    abstract val localPref: Int
    abstract val asPath: Path<BGPNode>

    companion object Factories {

        fun with(localPref: Int, asPath: Path<BGPNode>): BGPRoute {
            return ValidBGPRoute(localPref, asPath)
        }

        fun invalid(): BGPRoute {
            return InvalidBGPRoute
        }

    }

    /**
     * An implementation for a valid BGP route.
     */
    private data class ValidBGPRoute(override val localPref: Int, override val asPath: Path<BGPNode>) : BGPRoute() {
        override fun isValid(): Boolean = true
    }

    /**
     * An implementation for a invalid BGP route.
     */
    private object InvalidBGPRoute : BGPRoute() {
        override val localPref: Int = Int.MIN_VALUE
        override val asPath: Path<BGPNode> = emptyPath()
        override fun isValid(): Boolean = false
    }

}