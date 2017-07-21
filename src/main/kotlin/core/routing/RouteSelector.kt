package core.routing

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 *
 * A route selector is responsible for selecting the best route in a routing table.
 * It also works as a cache for the routing table
 *
 */
class RouteSelector<N: Node, R: Route>
(private val table: RoutingTable<N, R>, forceReselect: Boolean = false, private val compare: (R, R) -> Int) {

    private var selectedRoute: R = table.invalidRoute
    private var selectedNeighbor: N? = null
    init {
        if (forceReselect) {
            val (route, neighbor) = reselect()
            updateSelectedTo(route, neighbor)
        }
    }

    fun getSelectedRoute(): R = selectedRoute

    fun getSelectedNeighbor(): N? = selectedNeighbor

    fun update(neighbor: N, route: R) {

        val neighborExists = table.update(neighbor, route)

        if (!neighborExists) return

        if (neighbor == selectedNeighbor && compare(route, selectedRoute) != 0) {
            val (newlySelectedRoute, newlySelectedNeighbor) = reselect()
            updateSelectedTo(newlySelectedRoute, newlySelectedNeighbor)

        } else if (compare(route, selectedRoute) > 0) {
            updateSelectedTo(route, neighbor)
        }
    }

    fun reselect(): Pair<R, N?> {

        var selectedRoute = table.invalidRoute
        var selectedNeighbor: N? = null

        table.forEach { neighbor, route -> if (compare(route, selectedRoute) > 0) {
                selectedRoute = route
                selectedNeighbor = neighbor
            }
        }

        return Pair(selectedRoute, selectedNeighbor)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline private fun updateSelectedTo(route: R, neighbor: N?) {
        selectedRoute = route
        selectedNeighbor = neighbor
    }

}