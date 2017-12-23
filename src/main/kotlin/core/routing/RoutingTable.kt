package core.routing

/**
 * A RoutingTable is a data structure that associates neighbors to routes.
 *
 * A routing table stores an entry for each neighbor. Each entry stores a candidate route and a set
 * of attributes. Currently, the only attribute supported is a flag indicating whether or not the
 * corresponding neighbor was enabled or not.
 *
 * A routing table can hold a single type of route given by [R]. Given that different route
 * implementations have different definitions for an invalid route, the routing table requires a
 * [invalidRoute] to use as invalid route. The [invalidRoute] is assigned to a neighbor when the
 * table does not hold any candidate route for that neighbor.
 *
 * By default, all neighbors are associated with the [invalidRoute] and are enabled.
 *
 * The routing table does not perform any selection operations over its routes. For that see
 * [RouteSelector].
 *
 * @property invalidRoute the route to use as invalid route
 * @property size         the number of route entries stored in this routing table
 *
 * Created on 21-07-2017
 *
 * @author David Fialho
 */
class RoutingTable<R: Route> private constructor(
        val invalidRoute: R,
        private val routes: MutableMap<Node<R>, MutableEntry<R>> = HashMap()
) {

    companion object Factory {

        /**
         * Returns a routing table without any entries. The route [invalid] will be used as the
         * invalid route.
         */
        fun <R: Route> empty(invalid: R) = RoutingTable(invalid)

        /**
         * Returns a routing table containing the specified [entries]. The route [invalid] will be
         * used as the invalid route.
         */
        fun <R: Route> of(invalid: R, vararg entries: Entry<R>): RoutingTable<R> {

            val routes = HashMap<Node<R>, MutableEntry<R>>(entries.size)
            for ((neighbor, route, enabled) in entries) {
                routes.put(neighbor, MutableEntry(route, enabled))
            }

            return RoutingTable(invalid, routes)
        }

    }

    /**
     * Returns the number of entries in the table.
     */
    val size: Int get() = routes.size

    /**
     * Entries that are actually stored. Entries need to mutable for the table to update its
     * attributes individually.
     */
    data class MutableEntry<R>(var route: R, var enabled: Boolean = true)

    /**
     * Represents an entry in the routing table.
     */
    data class Entry<R: Route>(val neighbor: Node<R>, val route: R, val enabled: Boolean = true)

    /**
     * Returns the candidate route stored for [neighbor].
     */
    operator fun get(neighbor: Node<R>): R {
        return routes[neighbor]?.route ?: invalidRoute
    }

    /**
     * Sets the candidate route for [neighbor].
     */
    operator fun set(neighbor: Node<R>, route: R) {

        val entry = routes[neighbor]

        if (entry == null) {
            // It is a new neighbor - create a new entry
            routes[neighbor] = MutableEntry(route)
        } else {
            // Update the existing entry
            entry.route = route
        }

    }

    /**
     * Clears all entries from the table. All neighbors are enabled and assigned the [invalidRoute].
     */
    fun clear() {
        routes.clear()
    }

    /**
     * Sets the enable/disable flag for [neighbor], according to [enabled].
     *
     * @return the candidate route for [neighbor]
     */
    fun setEnabled(neighbor: Node<R>, enabled: Boolean): R {
        val entry = routes[neighbor]

        return if (entry == null) {
            routes[neighbor] = MutableEntry(invalidRoute, enabled = false)
            invalidRoute

        } else {
            entry.enabled = enabled
            entry.route
        }
    }

    /**
     * Checks whether or not [neighbor] is enabled. By default, a neighbor is enabled.
     */
    fun isEnabled(neighbor: Node<R>): Boolean {
        return routes[neighbor]?.enabled ?: return true
    }

    /**
     * Applies [operation] to each entry in this routing table.
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