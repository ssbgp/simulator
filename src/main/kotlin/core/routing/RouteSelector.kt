package core.routing

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 *
 * A route selector is responsible for selecting the best route in a routing table.
 * It also works as a cache for the routing table
 */
interface RouteSelector<N, R> {

    val selectedRoute: R
    val selectedNeighbor: N?

    fun update(neighbor: N, route: R)

    fun hasSelectedNewRoute(): Boolean

    fun disable(neighbor: N)

}