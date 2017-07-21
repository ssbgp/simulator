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
object RouteSelectorTests : Spek({

    //region Helper methods

    /**
     * Returns a route selector for testing based on fake nodes and fake routes.
     */
    fun routeSelector(table: RoutingTable<Node, Route>): RouteSelector<Node, Route> {
        return RouteSelector(table, forceReselect = true, compare = ::fakeCompare)
    }

    /**
     * Returns an initialized routing table based on the pairs that are provided.
     */
    fun table(vararg routes: Pair<Node, Route>): RoutingTable<Node, Route> {

        val neighbors = routes.map { it.first }.toList()
        val table = RoutingTable(invalidRoute(), neighbors)

        for ((neighbor, route) in routes) {
            table[neighbor] = route
        }

        return table
    }

    /**
     * Allows us to write something like 'route(1) via node(1)'
     */
    infix fun Route.via(neighbor: Node): Pair<Node, Route> {
        return Pair(neighbor, this)
    }

    //endregion

    given("a route selector using a table with no neighbors") {

        val selector = routeSelector(table())

        it("selects an invalid route") {
            assertThat(selector.getSelectedRoute(), `is`(invalidRoute()))
        }

        it("selects a null neighbor") {
            assertThat(selector.getSelectedNeighbor(), `is`(nullValue()))
        }

        on("updating the route of some neighbor with ID 1 to route with preference 10") {

            val updated = selector.update(node(1), route(preference = 10))

            it("still selects an invalid route") {
                assertThat(selector.getSelectedRoute(), `is`(invalidRoute()))
            }

            it("still selects a null neighbor") {
                assertThat(selector.getSelectedNeighbor(), `is`(nullValue()))
            }

            it("indicates the selected route/neighbor was NOT updated") {
                assertThat(updated, `is`(false))
            }
        }
    }

    given("a route selector using a table containing a route with preference 10 via a neighbor with ID 1") {

        val selector = routeSelector(table(
                route(preference = 10) via node(1)
        ))

        it("selects route with preference 10") {
            assertThat(selector.getSelectedRoute(), `is`(route(preference = 10)))
        }

        it("selects neighbor with ID 1") {
            assertThat(selector.getSelectedNeighbor(), `is`(node(1)))
        }

        on("updating the route neighbor with ID 1 to route with preference 15") {

            val updated = selector.update(node(1), route(preference = 15))

            it("selects route with preference 15") {
                assertThat(selector.getSelectedRoute(), `is`(route(preference = 15)))
            }

            it("selects neighbor with ID 1") {
                assertThat(selector.getSelectedNeighbor(), `is`(node(1)))
            }

            it("indicates the selected route/neighbor was updated") {
                assertThat(updated, `is`(true))
            }
        }

        on("updating the route of neighbor with ID 1 to invalid route") {

            val updated = selector.update(node(1), invalidRoute())

            it("selects route invalid route") {
                assertThat(selector.getSelectedRoute(), `is`(invalidRoute()))
            }

            it("selects null neighbor") {
                assertThat(selector.getSelectedNeighbor(), `is`(nullValue()))
            }

            it("indicates the selected route/neighbor was updated") {
                assertThat(updated, `is`(true))
            }
        }

        on("updating the route of neighbor with ID 1 to invalid route again") {

            val updated = selector.update(node(1), invalidRoute())

            it("selects route invalid route") {
                assertThat(selector.getSelectedRoute(), `is`(invalidRoute()))
            }

            it("selects null neighbor") {
                assertThat(selector.getSelectedNeighbor(), `is`(nullValue()))
            }

            it("indicates the selected route/neighbor was NOT updated") {
                assertThat(updated, `is`(false))
            }
        }

    }

    given("a route selector using a table containing invalid routes via neighbors 1 and 2") {

        val selector = routeSelector(table(
                invalidRoute() via node(1),
                invalidRoute() via node(2)
        ))

        it("selects an invalid route") {
            assertThat(selector.getSelectedRoute(), `is`(invalidRoute()))
        }

        it("selects a neighbor that is null") {
            assertThat(selector.getSelectedNeighbor(), `is`(nullValue()))
        }

        on("updating the route of neighbor 1 to route with preference 10") {

            val updated = selector.update(node(1), route(preference = 10))

            it("selects route with preference 10") {
                assertThat(selector.getSelectedRoute(), `is`(route(preference = 10)))
            }

            it("selects neighbor 1") {
                assertThat(selector.getSelectedNeighbor(), `is`(node(1)))
            }

            it("indicates the selected route/neighbor was updated") {
                assertThat(updated, `is`(true))
            }

        }

        on("updating the route of neighbor 1 to route with preference 5") {

            val updated = selector.update(node(1), route(preference = 5))

            it("selects route with preference 5") {
                assertThat(selector.getSelectedRoute(), `is`(route(preference = 5)))
            }

            it("selects neighbor 1") {
                assertThat(selector.getSelectedNeighbor(), `is`(node(1)))
            }

            it("indicates the selected route/neighbor was updated") {
                assertThat(updated, `is`(true))
            }

        }

        on("updating the route of neighbor 2 to route with preference 15") {

            val updated = selector.update(node(2), route(preference = 15))

            it("selects route with preference 15") {
                assertThat(selector.getSelectedRoute(), `is`(route(preference = 15)))
            }

            it("selects neighbor 2") {
                assertThat(selector.getSelectedNeighbor(), `is`(node(2)))
            }

            it("indicates the selected route/neighbor was updated") {
                assertThat(updated, `is`(true))
            }

        }

        on("updating the route of neighbor 2 to route with preference 1") {

            val updated = selector.update(node(2), route(preference = 1))

            it("selects route with preference 5") {
                assertThat(selector.getSelectedRoute(), `is`(route(preference = 5)))
            }

            it("selects neighbor 1") {
                assertThat(selector.getSelectedNeighbor(), `is`(node(1)))
            }

            it("indicates the selected route/neighbor was updated") {
                assertThat(updated, `is`(true))
            }

        }

        on("updating the route of neighbor 2 to route with preference 3") {

            val updated = selector.update(node(2), route(preference = 3))

            it("selects route with preference 5") {
                assertThat(selector.getSelectedRoute(), `is`(route(preference = 5)))
            }

            it("selects neighbor 1") {
                assertThat(selector.getSelectedNeighbor(), `is`(node(1)))
            }

            it("indicates the selected route/neighbor was NOT updated") {
                assertThat(updated, `is`(false))
            }

        }
    }

    given("a route selector using a table containing valid routes via neighbors 1 and 2 and selecting route via 1") {

        val selector = routeSelector(table(
                route(preference = 10) via node(1),
                route(preference = 5) via node(2)
        ))

        on("disabling neighbor 1") {

            val updated = selector.disable(node(1))

            it("indicates the selected route/neighbor was updated") {
                assertThat(updated, `is`(true))
            }

            it("selects route via neighbor 2") {
                assertThat(selector.getSelectedRoute(), `is`(route(preference = 5)))
            }

            it("selects neighbor 2") {
                assertThat(selector.getSelectedNeighbor(), `is`(node(2)))
            }

        }

        on("updating route via neighbor 1 to a route with preference 15") {

            val updated = selector.update(node(1), route(preference = 15))

            it("indicates the selected route/neighbor was NOT updated") {
                assertThat(updated, `is`(false))
            }

            it("selects route via neighbor 2") {
                assertThat(selector.getSelectedRoute(), `is`(route(preference = 5)))
            }

            it("selects neighbor 2") {
                assertThat(selector.getSelectedNeighbor(), `is`(node(2)))
            }

        }

        on("enabling neighbor 1") {

            val updated = selector.enable(node(1))

            it("indicates the selected route/neighbor was updated") {
                assertThat(updated, `is`(true))
            }

            it("selects route with preference 15") {
                assertThat(selector.getSelectedRoute(), `is`(route(preference = 15)))
            }

            it("selects neighbor 1") {
                assertThat(selector.getSelectedNeighbor(), `is`(node(1)))
            }

        }

        on("disabling neighbor 2") {

            val updated = selector.disable(node(2))

            it("indicates the selected route/neighbor was NOT updated") {
                assertThat(updated, `is`(false))
            }

            it("selects route with preference 15") {
                assertThat(selector.getSelectedRoute(), `is`(route(preference = 15)))
            }

            it("selects neighbor 1") {
                assertThat(selector.getSelectedNeighbor(), `is`(node(1)))
            }

        }

        on("enabling neighbor 2") {

            val updated = selector.enable(node(2))

            it("indicates the selected route/neighbor was NOT updated") {
                assertThat(updated, `is`(false))
            }

            it("selects route with preference 15") {
                assertThat(selector.getSelectedRoute(), `is`(route(preference = 15)))
            }

            it("selects neighbor 1") {
                assertThat(selector.getSelectedNeighbor(), `is`(node(1)))
            }

        }

        on("disabling neighbors 1 and 2") {

            selector.disable(node(1))
            selector.disable(node(2))

            it("selects invalid route") {
                assertThat(selector.getSelectedRoute(), `is`(invalidRoute()))
            }

            it("selects null neighbor") {
                assertThat(selector.getSelectedNeighbor(), `is`(nullValue()))
            }

        }

    }

})