package bgp.policies.shortestpath

import bgp.BGPRoute
import core.routing.Extender
import core.routing.Node
import core.routing.pathOf

/**
 * Created on 24-07-2017.
 *
 * @author David Fialho
 */
class ShortestPathExtender(val cost: Int) : Extender<BGPRoute> {

    /**
     * Extends the route by adding the cost of the extender to the LOCAL-PREF of the incoming route.
     */
    override fun extend(route: BGPRoute, sender: Node<BGPRoute>): BGPRoute {

        return when {
            !route.isValid() -> BGPRoute.invalid()
            BGPRoute.self() == route -> BGPRoute.with(localPref = cost, asPath = pathOf(sender))
            else -> BGPRoute.with(localPref = route.localPref + cost, asPath = route.asPath.append(sender))
        }

    }

}