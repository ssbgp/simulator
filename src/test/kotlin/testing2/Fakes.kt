package testing2

import core.routing2.*

sealed class FakeRoute: Route {

    abstract val preference: Int

    internal data class ValidFakeRoute(override val preference: Int): FakeRoute() {
        override fun isValid() = true
    }

    internal object InvalidFakeRoute: FakeRoute() {
        override val preference = Int.MIN_VALUE
        override fun isValid() = false
    }
}

/**
 * A compare method for fake routes.
 *
 * Not surprisingly, it considers routes with higher preference value to be preferred to routes with lower
 * preference values.
 */
fun fakeCompare(route1: Route, route2: Route): Int {
    route1 as FakeRoute
    route2 as FakeRoute

    return route1.preference.compareTo(route2.preference)
}

object FakeProtocol: Protocol<Route> {

    override fun start() {
        TODO("not implemented")
    }

    override fun processIt(message: Message<Route>) {
        TODO("not implemented")
    }
}

//region Factory methods

/**
 * Returns a node with the specified ID using a fake protocol.
 */
fun node(id: NodeID): Node<Route> {
    return Node(id, FakeProtocol)
}

/**
 * Returns a valid route with the given preference value.
 */
fun route(preference: Int): Route = FakeRoute.ValidFakeRoute(preference)

/**
 * Returns an invalid route.
 */
fun invalidRoute(): Route = FakeRoute.InvalidFakeRoute

//endregion