package core.routing2

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
class RoutingTable<R: Route>
private constructor(val invalidRoute: R, private val routes: MutableMap<Node<R>, EntryData<R>> = HashMap()) {

    /**
     * Returns the number of entries in the table.
     */
    val size: Int get() = routes.size

    /**
     * Contains the data stored in each entry.
     */
    data class EntryData<R>(var route: R, var enabled: Boolean = true)

    /**
     * Represents an entry in the routing table.
     */
    data class Entry<R: Route>(val neighbor: Node<R>, val route: R, val enabled: Boolean = true)

    companion object Factory {

        /**
         * Returns a routing table with no entries.
         */
        fun <R: Route> empty(invalid: R) = RoutingTable(invalid)

        /**
         * Returns a routing table containing the specified entries.
         */
        fun <R: Route> of(invalid: R, vararg entries: Entry<R>): RoutingTable<R> {

            val routes = HashMap<Node<R>, EntryData<R>>(entries.size)
            for ((neighbor, route, enabled) in entries) {
                routes.put(neighbor, EntryData(route, enabled))
            }

            return RoutingTable(invalid, routes)
        }

    }

    /**
     * Returns the candidate route via a neighbor.
     */
    operator fun get(neighbor: Node<R>): R {
        return routes[neighbor]?.route ?: invalidRoute
    }

    /**
     * Sets the candidate route via a neighbor.
     * If the given node is not defined as a neighbor, then the table is not modified.
     */
    operator fun set(neighbor: Node<R>, route: R) {

        val entry = routes[neighbor]

        if (entry == null) {
            routes[neighbor] = EntryData(route)
        } else {
            entry.route = route
        }

    }

    /**
     * Clears all entries from the table.
     */
    fun clear() {
        routes.clear()
    }

    /**
     * Sets the enable/disable flag for the given neighbor.
     *
     * @return the route via the specified neighbor or invalid route if the table contains no entry for that neighbor
     */
    fun setEnabled(neighbor: Node<R>, enabled: Boolean): R {
        val entry = routes[neighbor]

        return if (entry == null) {
            routes[neighbor] = EntryData(invalidRoute, enabled = false)
            invalidRoute

        } else {
            entry.enabled = enabled
            entry.route
        }
    }

    /**
     * Checks if a neighbor is enabled or not. If the table contains no entry for the specified neighbor it indicates
     * the neighbor is enabled.
     */
    fun isEnabled(neighbor: Node<R>): Boolean {
        return routes[neighbor]?.enabled ?: return true
    }

    /**
     * Provides way to iterate over each entry in the table.
     */
    inline internal fun forEach(operation: (Node<R>, R, Boolean) -> Unit) {
        for ((neighbor, entry) in routes) {
            operation(neighbor, entry.route, entry.enabled)
        }
    }

    override fun toString(): String {
        return "RoutingTable(routes=$routes)"
    }

}