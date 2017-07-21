package core.routing

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 *
 * The routing table stores a candidate route for each defined out-neighbor.
 * It does not perform any route selection! For that use the RouteSelector.
 */
class RoutingTable<N: Node, R: Route>(val invalidRoute: R, neighbors: Collection<N> = emptyList()) {

    private val routes = HashMap<N, R>(neighbors.size)
    init {
        neighbors.forEach { routes[it] = invalidRoute }
    }

    /**
     * Returns the candidate route via a neighbor.
     */
    operator fun get(neighbor: N): R {
        return routes[neighbor] ?: invalidRoute
    }

    /**
     * Sets the candidate route via a neighbor.
     * If the given node is not defined as a neighbor, then the table is not modified.
     */
    operator fun set(neighbor: N, route: R) {
        routes.replace(neighbor, route)
    }

    /**
     * Sets the candidate route via a neighbor.
     * If the given node is not defined as a neighbor, then the table is not modified and the method returns false.
     *
     * @return true if the neighbor is defined and false if otherwise
     */
    fun update(neighbor: N, route: R): Boolean {
        return routes.replace(neighbor, route) != null
    }

    /**
     * Sets invalid routes for all defined neighbors.
     */
    fun clear() {
        // Sets invalid route for all neighbors
        routes.replaceAll { _,_ -> invalidRoute }
    }

    /**
     * Provides way to iterate over each entry in the table.
     */
    internal fun forEach(operation: (N, R) -> Unit) {
        for (entry in routes) {
            operation(entry.key, entry.value)
        }
    }

}