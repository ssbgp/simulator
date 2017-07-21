package testing

import core.routing.Route

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 *
 * This file contains a fake implementation for the route interface to be used in the test. It also includes some
 * methods to obtain a route from this implementation.
 *
 * Fake route implementation used to test the routing table. A fake route has a single integer attribute called
 * preference.
 */
sealed class FakeRoute : Route {

    abstract val preference: Int

    internal data class ValidFakeRoute(override val preference: Int) : FakeRoute() {
        override fun isValid() = true
    }

    internal object InvalidFakeRoute : FakeRoute() {
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

//region Factory methods

/**
 * Returns a valid route with the given preference value.
 */
fun route(preference: Int): Route = FakeRoute.ValidFakeRoute(preference)

/**
 * Returns an invalid route.
 */
fun invalidRoute(): Route = FakeRoute.InvalidFakeRoute

//endregion