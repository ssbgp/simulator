package core.routing

/**
 * A RouteSelector is responsible for selecting the most preferred route in a routing [table],
 * according to a [compare] function.
 *
 * The [compare] function should take two routes and return an integer value to indicate their
 * order:
 *   - a positive integer value indicates the first route has an higher preference than the second
 *   - a zero indicates the two routes have the exact same preference
 *   - a negative integer value indicates the first route has an lower preference than the second
 *
 * The way it works is similar to a cache. It keeps track of the selected route and neighbor until
 * these become invalid. Instead of updating the routing table directly, you use the [update]
 * method, which triggers the route selection operation and updates the routing table as well. This
 * allows the selector to adjust the selected route and neighbor in the most efficient way
 * possible. Warning: DO NOT update the routing table outside of the selector, since doing this
 * will hide routes from the selector, which will probably lead to unexpected behavior.
 *
 * @property table the underlying routing table that actually stores the routes
 *
 * @constructor Creates a new selector around the given [table]. It should not be used directly.
 * Use the factory methods instead.
 * @param forceReselect if true, then the selector inspects all routes in the table and selects
 * the most preferred route. Otherwise, the selector starts off with no selected route.
 *
 * Created on 21-07-2017
 *
 * @author David Fialho
 */
class RouteSelector<R : Route> private constructor(
        val table: RoutingTable<R>,
        private val compare: (R, R) -> Int,
        forceReselect: Boolean = true
) {

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

    companion object Factory {

        /**
         * Returns a [RouteSelector] based on newly created routing table. This is the
         * recommended way to create a selector from a new table.
         *
         * @param invalid the route to set as invalid route by the new routing table
         * @param compare the function to compare routes with
         */
        fun <R : Route> wrapNewTable(invalid: R, compare: (R, R) -> Int): RouteSelector<R> {
            return RouteSelector(RoutingTable.empty(invalid), compare, forceReselect = false)
        }

        /**
         * Returns a [RouteSelector] wrapping an existing routing [table]. Upon initialization the
         * selector goes through all routes stored in [table] and selects the best route
         * according to [compare].
         *
         * @param table   the table to be wrapped by the selector
         * @param compare the compare method used to compare route preferences
         */
        fun <R : Route> wrap(table: RoutingTable<R>, compare: (R, R) -> Int): RouteSelector<R> {
            return RouteSelector(table, compare)
        }

    }

    /**
     * Returns the currently selected route
     */
    // TODO @refactor - use a value property instead of the get method
    fun getSelectedRoute(): R = selectedRoute

    /**
     * Returns the currently selected neighbor.
     */
    // TODO @refactor - use a value property instead of the get method
    fun getSelectedNeighbor(): Node<R>? = selectedNeighbor

    /**
     * Updates the candidate route for [neighbor] to [route].
     *
     * This operation may trigger an update to the selected route and neighbor.
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
     * Disables [neighbor]. Disabling a neighbor makes that neighbor and its respective candidate
     * route non-eligible for selection, even if that route is the best route available. That is,
     * the selector will never select a route from a disabled neighbor.
     *
     * This operation may trigger an update to the selected route and neighbor.
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
     * Enables [neighbor] if [neighbor] was disabled. Enabling a disabled neighbor may trigger
     * an update to the selected route and neighbor. Enabling an already enabled neighbor has no
     * effect.
     *
     * @return true if the selected route/neighbor was updated or false if otherwise
     */
    fun enable(neighbor: Node<R>): Boolean {

        table.setEnabled(neighbor, true)

        // Checking if the neighbor was really removed from the disabled set avoids making
        // a table lookup if the node was not disabled

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
    // TODO @refactor - remove this method because it is not used in production
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
     * Forces the selector to re-evaluate all candidate route and re-select best route among them.
     */
    fun reselect() {

        selectedRoute = table.invalidRoute
        selectedNeighbor = null

        table.forEach { neighbor, route, enabled ->
            if (enabled && compare(route, selectedRoute) > 0) {
                selectedRoute = route
                selectedNeighbor = neighbor
            }
        }
    }

    /**
     * Clears all routes from the underlying routing table. The selector automatically updates
     * its selected route to the invalid route.
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