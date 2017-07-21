package core.routing

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.jetbrains.spek.api.dsl.on
import testing.*

/**
 * Created on 21-07-2017

 * @author David Fialho
 */
object BaseRouteSelectorTests : Spek({

    given("a clean route selector") {

        val selector = routeSelector()

        it("has selected an invalid route") {
            assertThat(selector.selectedRoute, `is`(invalidRoute()))
        }

        it("has selected a neighbor that is null") {
            assertThat(selector.selectedNeighbor, `is`(nullValue()))
        }

        on("updating the route of neighbor 1 to route with preference 10") {

            selector.update(node(1), route(preference = 10))

            it("has selected route with preference 10") {
                assertThat(selector.selectedRoute, `is`(route(preference = 10)))
            }

            it("has selected neighbor 1") {
                assertThat(selector.selectedNeighbor, `is`(node(1)))
            }

        }

        on("updating the route of neighbor 1 to route with preference 5") {

            selector.update(node(1), route(preference = 5))

            it("has selected route with preference 5") {
                assertThat(selector.selectedRoute, `is`(route(preference = 5)))
            }

            it("has selected neighbor 1") {
                assertThat(selector.selectedNeighbor, `is`(node(1)))
            }

        }

        on("updating the route of neighbor 2 to route with preference 15") {

            selector.update(node(2), route(preference = 15))

            it("has selected route with preference 15") {
                assertThat(selector.selectedRoute, `is`(route(preference = 15)))
            }

            it("has selected neighbor 2") {
                assertThat(selector.selectedNeighbor, `is`(node(2)))
            }

        }

        on("updating the route of neighbor 2 to route with preference 1") {

            selector.update(node(2), route(preference = 1))

            it("has selected route with preference 5") {
                assertThat(selector.selectedRoute, `is`(route(preference = 5)))
            }

            it("has selected neighbor 1") {
                assertThat(selector.selectedNeighbor, `is`(node(1)))
            }

        }
    }

})

//region Fake implementation of RouteSelector used in the tests

/**
 * This is a fake implementation for the BaseRouteSelector class to be used for testing. It assumes the nodes and
 * routes are FakeNode and FakeRoute, respectively.
 *
 * Not surprisingly, it considers routes with higher preference value to be preferred to routes with lower preference
 * values.
 */
class FakeRouteSelector : BaseRouteSelector<Node, Route>(FakeRoute.InvalidFakeRoute) {

    override fun compare(route1: Route, route2: Route): Int {
        route1 as FakeRoute
        route2 as FakeRoute

        return route1.preference.compareTo(route2.preference)
    }
}

/**
 * Returns a route selector for testing.
 */
fun routeSelector(): RouteSelector<Node, Route> = FakeRouteSelector()

//endregion