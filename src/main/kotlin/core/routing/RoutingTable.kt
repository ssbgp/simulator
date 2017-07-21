package core.routing

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 *
 * The routing table stores a candidate route for each defined out-neighbor.
 * For each neighbor it holds a flag indicating if the neighbor is enabled or not. If the neighbor is set as disabled
 * the route associated with that neighbor should not ever be selected.
 *
 * It does not perform any route selection! For that use the RouteSelector.
 */
class RoutingTable<N: Node, R: Route>(val invalidRoute: R, neighbors: Collection<N> = emptyList()) {

    data class Entry<R>(var route: R, var enabled: Boolean)

    private val routes = HashMap<N, Entry<R>>(neighbors.size)
    init {
        // By default, all neighbors are enabled
        neighbors.forEach { routes[it] = Entry(invalidRoute, enabled = true) }
    }

    /**
     * Returns the candidate route via a neighbor.
     */
    operator fun get(neighbor: N): R {
        return routes[neighbor]?.route ?: invalidRoute
    }

    /**
     * Sets the candidate route via a neighbor.
     * If the given node is not defined as a neighbor, then the table is not modified.
     */
    operator fun set(neighbor: N, route: R) {
        routes[neighbor]?.route = route
    }

    /**
     * Sets the candidate route via a neighbor.
     * If the given node is not defined as a neighbor, then the table is not modified and the method returns false.
     *
     * @return true if the neighbor is defined and false if otherwise
     */
    fun update(neighbor: N, route: R): Boolean {
        val entry = routes[neighbor]

        if (entry != null) {
            entry.route = route
            return true
        } else {
            return false
        }
    }

    /**
     * Sets invalid routes for all defined neighbors an enables all disabled neighbors.
     */
    fun clear() {
        // Sets invalid route for all neighbors
        routes.forEach { _, entry -> entry.route = invalidRoute }
    }

    /**
     * Sets the enable/disable flag for the given neighbor.
     */
    fun setEnabled(neighbor: N, enabled: Boolean) {
        routes[neighbor]?.enabled = enabled
    }

    /**
     * Checks if a neighbor is enabled or not
     */
    fun isEnabled(neighbor: N): Boolean {
        return routes[neighbor]?.enabled ?: false
    }

    /**
     * Provides way to iterate over each entry in the table.
     */
    internal fun forEach(operation: (N, R, Boolean) -> Unit) {
        for ((neighbor, entry) in routes) {
            operation(neighbor, entry.route, entry.enabled)
        }
    }

}