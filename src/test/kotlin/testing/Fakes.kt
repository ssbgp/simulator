package testing

import core.routing.*

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

    override fun start(node: Node<Route>) {
        TODO("not implemented")
    }

    override fun process(message: Message<Route>) {
        TODO("not implemented")
    }

    override fun reset() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

/**
 * Created on 25-07-2017
 *
 * @author David Fialho
 *
 * Fake extender used for testing purposes.
 */
object FakeExtender: Extender<Route> {
    override fun extend(route: Route, sender: Node<Route>): Route = invalidRoute()
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

/**
 * Returns an extender when it is needed one but it is not important which one.
 */
fun someExtender(): Extender<Route> {
    return FakeExtender
}

//endregion