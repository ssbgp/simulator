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
    abstract val asPath: Path

    companion object Factory {

        /**
         * Returns a BGP route with the specified LOCAL-PREF and AS-PATH.
         */
        fun with(localPref: Int, asPath: Path): BGPRoute = ValidBGPRoute(localPref, asPath)

        /**
         * Returns an invalid BGP route.
         */
        fun invalid(): BGPRoute = InvalidBGPRoute

        /**
         * Returns a self BGP route. A self BGP route is the BGP route with the highest preference possible.
         */
        fun self(): BGPRoute = SelfBGPRoute

    }

    /**
     * An implementation for a valid BGP route.
     */
    private data class ValidBGPRoute(override val localPref: Int, override val asPath: Path) : BGPRoute() {
        override fun isValid(): Boolean = true
    }

    /**
     * An implementation for a invalid BGP route.
     */
    private object InvalidBGPRoute : BGPRoute() {
        override val localPref: Int = Int.MIN_VALUE
        override val asPath: Path = emptyPath()
        override fun isValid(): Boolean = false
        override fun toString(): String = "•"
    }

    /**
     * An implementation for a self BGP route. A self BGP route is the BGP route with the highest preference possible.
     */
    private object SelfBGPRoute : BGPRoute() {
        override val localPref: Int = Int.MAX_VALUE
        override val asPath: Path = emptyPath()
        override fun isValid(): Boolean = true
        override fun toString(): String = "◦"
    }

    override fun toString(): String {
        return "BGPRoute(localPref=$localPref, asPath=$asPath)"
    }

    /**
     * Two BGP routes are considered equal if and only if they have the same exact local preference value and the
     * same AS path
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BGPRoute

        if (localPref != other.localPref) return false

        // trick to avoid having to compare each node of the AS-paths most times
        if (asPath.size != other.asPath.size) return false

        if (asPath != other.asPath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = localPref
        result = 31 * result + asPath.hashCode()
        return result
    }


}

/**
 * Compare function for BGP routes. It compares the preference of two BGP routes.
 *
 * The preference of a BGP route is determined based on the following attributes:
 *
 *  1. the LOCAL-PREF
 *  2. the length of the AS-PATH
 *  3. the ID of the next-hop node
 *
 * @return positive value if route1 is preferred to route 2; zero if they have the same preference; and negative
 * value if route2 is preferred to route1
 */
fun bgpRouteCompare(route1: BGPRoute, route2: BGPRoute): Int {

    var difference = route1.localPref.compareTo(route2.localPref)
    if (difference == 0) {
        difference = route2.asPath.size.compareTo(route1.asPath.size)
        if (difference == 0) {

            val nextHop1 = route1.asPath.nextHop() ?: return 0
            val nextHop2 = route2.asPath.nextHop() ?: return 0

            difference = nextHop2.id.compareTo(nextHop1.id)
        }
    }

    return difference
}
