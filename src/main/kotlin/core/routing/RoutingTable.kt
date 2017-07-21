package core.routing

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 */
class RoutingTable<N: Node, R: Route>(val invalidRoute: R, neighbors: Collection<N> = emptyList()) {

    private val routes = HashMap<N, R>(neighbors.size)
    init {
        neighbors.forEach { routes[it] = invalidRoute }
    }

    operator fun get(neighbor: N): R {
        return routes[neighbor] ?: invalidRoute
    }

    operator fun set(neighbor: N, route: R) {
        routes.replace(neighbor, route)
    }

    fun clear() {
        // Sets invalid route for all neighbors
        routes.replaceAll { _,_ -> invalidRoute }
    }

    internal fun forEach(operation: (N, R) -> Unit) {
        for (route in routes) {
            operation(route.key, route.value)
        }
    }

}