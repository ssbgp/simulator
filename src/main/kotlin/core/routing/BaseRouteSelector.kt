package core.routing

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 */
abstract class BaseRouteSelector<N, R>(protected val defaultRoute: R) : RouteSelector<N, R> {

    private val routes = HashMap<N, R>()
    private var hasSelectedNewRoute = true

    private var route = defaultRoute
    private var neighbor: N? = null

    override var selectedRoute: R
        get() {
            hasSelectedNewRoute = false
            return route
        }
        protected set(value) {
            route = value
        }

    override var selectedNeighbor: N?
        get() {
            hasSelectedNewRoute = false
            return neighbor
        }
        protected set(value) {
            neighbor = value
        }

    override fun update(neighbor: N, route: R) {
        routes[neighbor] = route

        if (neighbor == selectedNeighbor && compare(route, selectedRoute) != 0) {
            val (newlySelectedRoute, newlySelectedNeighbor) = reselect()
            updateSelected(newlySelectedRoute, newlySelectedNeighbor)

        } else if (compare(route, selectedRoute) > 0) {
            updateSelected(route, neighbor)
        }

    }

    override fun hasSelectedNewRoute() = hasSelectedNewRoute

    override fun disable(neighbor: N) {
        TODO("not implemented")
    }

    abstract protected fun compare(route1: R, route2: R): Int

    private fun updateSelected(route: R, neighbor: N?) {
        this.route = route
        this.neighbor = neighbor
        hasSelectedNewRoute = true
    }

    private fun reselect(): Pair<R, N?> {

        var selectedRoute = defaultRoute
        var selectedNeighbor: N? = null

        for ((neighbor, route) in routes) if (compare(route, selectedRoute) > 0) {
            selectedRoute = route
            selectedNeighbor = neighbor
        }

        return Pair(selectedRoute, selectedNeighbor)
    }

}