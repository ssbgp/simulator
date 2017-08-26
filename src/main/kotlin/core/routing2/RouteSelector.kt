package core.routing2

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
 * The constructor takes the routing table holding the routes, a forceSelect flag, and a compare method. If the force
 * reselect flag is set to true it will force the selector to reselect the route/neighbor based on the initial routes of
 * the given table. By default, the flag is set to true. This flag should be set to false if and only if you are
 * sure the table contains only invalid routes. The compare method should take to routes and compare their
 * preferences. It should as a common compare method which returns a positive value if the left route has higher
 * preference than the right route, 0 if they have the same preference and a negative value if the left route has a
 * lower preference than the right route.
 *
 * @param table         the table to select routes from
 * @param forceReselect if set to true the selector will perform a reselect operation in the initializer
 * @param compare       the method used by the selector to compare the routes
 */
class RouteSelector<R: Route> private constructor
(val table: RoutingTable<R>, private val compare: (R, R) -> Int, forceReselect: Boolean = true) {

    companion object Factory {

        /**
         * Returns a RouteSelector wrapping a newly created routing table.
         *
         * @param invalid the invalid route
         * @param compare the compare method used to compare route preferences
         */
        fun <R: Route> wrapNewTable(invalid: R, compare: (R, R) -> Int): RouteSelector<R> {
            return RouteSelector(
                    table = RoutingTable.empty(invalid),
                    compare = compare,
                    forceReselect = false)
        }

        /**
         * Returns a RouteSelector wrapping an existing routing table.
         *
         * @param table   the table to be wrapped by the selector
         * @param compare the compare method used to compare route preferences
         */
        fun <R: Route> wrap(table: RoutingTable<R>, compare: (R, R) -> Int): RouteSelector<R> {
            return RouteSelector(table, compare)
        }

    }

    // Stores the currently selected route
    private var selectedRoute: R = table.invalidRoute
    // Stores the currently selected neighbor
    private var selectedNeighbor: Node<R>? = null

    /**
     * Keeps record of the neighbors that are disabled.
     */
    private val mutableDisabledNeighbors = HashSet<Node<R>>()
    val disabledNeighbors: Collection<Node<R>> get() = mutableDisabledNeighbors

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
    fun getSelectedNeighbor(): Node<R>? = selectedNeighbor

    /**
     * This method should always be used to update the routing table when a selector is being used.
     *
     * Updates the routing table, setting the given route as the candidate route via the given neighbor.
     * The selected route/neighbor may also be updated if the given route/neighbor forces a reselection.
     *
     * @return true if the selected route/neighbor was updated or false if otherwise
     */
    fun update(neighbor: Node<R>, route: R): Boolean {

        table[neighbor] = route

        return if (table.isEnabled(neighbor) && compare(route, selectedRoute) > 0) {
            updateSelectedTo(route, neighbor)
            true

        } else if (neighbor == selectedNeighbor && compare(route, selectedRoute) != 0) {
            reselect()
            true

        } else {
            // do nothing
            false
        }
    }

    /**
     * Disables a neighbor. Routes learned from a disabled neighbor are still stored in the routing table, but the
     * selector will never select a candidate route associated with that neighbor.
     *
     * @return true if the selected route/neighbor was updated or false if otherwise
     */
    fun disable(neighbor: Node<R>): Boolean {

        table.setEnabled(neighbor, false)
        mutableDisabledNeighbors.add(neighbor)

        // Do not need to check if the node was added to the disabled neighbors set:
        // if it wasn't then the neighbor was already disabled and surely is not the selected neighbor

        if (neighbor == selectedNeighbor) {
            reselect()
            return true
        }

        return false
    }

    /**
     * Enables a neighbor that was disabled. If the neighbor was not disabled than nothing changes.
     *
     * @return true if the selected route/neighbor was updated or false if otherwise
     */
    fun enable(neighbor: Node<R>): Boolean {

        table.setEnabled(neighbor, true)

        // Checking if the neighbor was really removed from the disabled set prevents making a table lookup
        // if the node was not disabled

        if (mutableDisabledNeighbors.remove(neighbor)) {


            val route = table[neighbor]

            if (compare(route, selectedRoute) > 0) {
                updateSelectedTo(route, neighbor)
                return true
            }
        }

        return false
    }

    /**
     * Enables all neighbors that are currently disabled.
     *
     * @return true if the selected route/neighbor was updated or false if otherwise
     */
    fun enableAll(): Boolean {

        var selectedRouteAmongDisabled = table.invalidRoute
        var selectedNeighborAmongDisabled: Node<R>? = null

        for (neighbor in mutableDisabledNeighbors) {
            val route = table.setEnabled(neighbor, true)

            if (compare(route, selectedRouteAmongDisabled) > 0) {
                selectedRouteAmongDisabled = route
                selectedNeighborAmongDisabled = neighbor
            }
        }

        // If we are enabling all neighbors that this set can be cleared
        mutableDisabledNeighbors.clear()

        if (compare(selectedRouteAmongDisabled, selectedRoute) > 0) {
            selectedRoute = selectedRouteAmongDisabled
            selectedNeighbor = selectedNeighborAmongDisabled
            return true
        } else {
            return false
        }
    }

    /**
     * Forces the selector to reselect the route/neighbor based on the current candidates routes available in the
     * routing table.
     */
    fun reselect() {

        selectedRoute = table.invalidRoute
        selectedNeighbor = null

        table.forEach { neighbor, route, enabled -> if (enabled && compare(route,
                selectedRoute) > 0) {
                selectedRoute = route
                selectedNeighbor = neighbor
            }
        }
    }

    /**
     * Clears the wrapped routing table.
     */
    fun clear() {
        selectedRoute = table.invalidRoute
        selectedNeighbor = null
        table.clear()
        mutableDisabledNeighbors.clear()
    }

    @Suppress("NOTHING_TO_INLINE")
    inline private fun updateSelectedTo(route: R, neighbor: Node<R>?) {
        selectedRoute = route
        selectedNeighbor = neighbor
    }

    override fun toString(): String {
        return "RouteSelector(table=$table)"
    }

}