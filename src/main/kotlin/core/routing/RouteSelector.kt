package core.routing

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 *
 * A route selector is responsible for selecting the best route in a routing table. It works similar to a cache: to
 * update the routing table the update() method of the selector should be used. This allows the selector to adjust
 * the selected route/neighbor in the most efficient way possible.
 *
 * DO NOT update the routing table outside of the selector. Doing so will prevent the selector from working correctly
 * since it is not informed of the changes performed to the table.
 *
 * The constructor takes the routing table holding the routes, a forceSelect flag, and a compare method. The force
 * reselect flag if true will force the selector to reselect the route/neighbor based on the current routes of the
 * given table. This flag should be set to true if and only if the table provided in the constructor already includes
 * valid routes. The compare method should take to routes and compare their preferences. It should as a common
 * compare method which returns a positive value if the left route has higher preference than the right route, 0 if
 * they have the same preference and a negative value if the left route has a lower preference than the right route.
 *
 * @param table         the table to select routes from
 * @param forceReselect if set to true the selector will perform a reselect operation in the initializer
 * @param compare       the method used by the selector to compare the routes
 */
class RouteSelector<N: Node, R: Route>
(private val table: RoutingTable<N, R>, forceReselect: Boolean = false, private val compare: (R, R) -> Int) {

    // Stores the currently selected route
    private var selectedRoute: R = table.invalidRoute
    // Stores the currently selected neighbor
    private var selectedNeighbor: N? = null

    init {
        if (forceReselect) {
            reselect()
        }
    }

    /**
     * Returns the currently selected route
     */
    fun getSelectedRoute(): R = selectedRoute

    /**
     * Returns the currently selected neighbor.
     */
    fun getSelectedNeighbor(): N? = selectedNeighbor

    /**
     * This method should always be used to update the routing table when a selector is being used.
     *
     * Updates the routing table, setting the given route as the candidate route via the given neighbor.
     * The selected route/neighbor may also be updated if the given route/neighbor forces a reselection.
     */
    fun update(neighbor: N, route: R) {

        val neighborExists = table.update(neighbor, route)

        if (!neighborExists) return

        if (neighbor == selectedNeighbor && compare(route, selectedRoute) != 0) {
            reselect()

        } else if (compare(route, selectedRoute) > 0) {
            updateSelectedTo(route, neighbor)
        }
    }

    /**
     * Forces the selector to reselect the route/neighbor based on the current candidates routes available in the
     * routing table.
     */
    fun reselect() {

        selectedRoute = table.invalidRoute
        selectedNeighbor = null

        table.forEach { neighbor, route -> if (compare(route, selectedRoute) > 0) {
                selectedRoute = route
                selectedNeighbor = neighbor
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline private fun updateSelectedTo(route: R, neighbor: N?) {
        selectedRoute = route
        selectedNeighbor = neighbor
    }

}