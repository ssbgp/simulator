package core.routing

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import testing.node

/**
 * Created on 21-07-2017

 * @author David Fialho
 */
object RoutingTableTest : Spek({

    //region Helper methods

    /**
     * Returns a valid route with the given preference value.
     */
    fun route(preference: Int): Route = ValidFakeRoute(preference)

    /**
     * Returns an invalid route.
     */
    fun invalidRoute(): Route = InvalidFakeRoute

    //endregion

    given("an empty routing table") {

        val table = RoutingTable<Node, Route>(invalidRoute())

        on("getting the route for any neighbor") {

            val neighborRoute = table[node(1)]

            it("returns an invalid route") {
                assertThat(neighborRoute.isValid(), `is`(false))
            }
        }

    }

    given("a routing table with neighbor with ID 1") {

        val table = RoutingTable(invalidRoute(), listOf(node(1)))

        on("getting the route for neighbor 1") {

            val neighborRoute = table[node(1)]

            it("returns an invalid route") {
                assertThat(neighborRoute.isValid(), `is`(false))
            }
        }

        on("setting route with preference 10 for neighbor 1") {

            table[node(1)] = route(preference = 10)

            it("returns a route with preference 10 when getting the route for neighbor 1") {
                assertThat(table[node(1)], `is`(route(preference = 10)))
            }
        }

    }

    given("a routing table with two neighbors with IDs 1 and 2") {

        val table = RoutingTable(invalidRoute(), listOf(node(1), node(2)))

        on("setting route with preference 10 for neighbor 1") {

            table[node(1)] = route(preference = 10)

            it("returns a valid route with preference 10 when getting the route for neighbor 1") {
                assertThat(table[node(1)], `is`(route(preference = 10)))
            }

            it("returns an invalid route when getting the route for neighbor 2") {
                assertThat(table[node(2)], `is`(invalidRoute()))
            }
        }

        on("setting valid route for neighbor not included in the table") {

            table[node(5)] = route(preference = 10)

            it("returns an invalid route when getting route for that neighbor") {
                // returning an invalid route indicates the neighbor was not added to the table
                assertThat(table[node(5)], `is`(invalidRoute()))
            }
        }

        on("clearing the table") {
            table.clear()

            it("returns an invalid route when getting the route for neighbor 1") {
                assertThat(table[node(1)], `is`(invalidRoute()))
            }

            it("returns an invalid route when getting the route for neighbor 2") {
                assertThat(table[node(2)], `is`(invalidRoute()))
            }
        }

        on("setting route with preference 10 to neighbor 1") {

            table[node(1)] = route(preference = 10)

            it("returns a valid route with preference 10 when getting the route for neighbor 1") {
                // this indicates the neighbors were not removed from the table when the table was cleared
                assertThat(table[node(1)], `is`(route(preference = 10)))
            }
        }

    }

})

//region Route implementation used for the tests

/**
 * Fake route implementation used to test the routing table. A fake route has a single integer attribute called
 * preference.
 */
interface FakeRoute : Route {
    val preference: Int
}

data class ValidFakeRoute(override val preference: Int) : FakeRoute {
    override fun isValid() = true
}

object InvalidFakeRoute : FakeRoute {
    override val preference = Int.MIN_VALUE
    override fun isValid() = false
}

//endregion
