package core.routing

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.hamcrest.Matchers.`is` as Is
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
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
    fun routeSelector(table: RoutingTable<Route>): RouteSelector<Route> {
        return RouteSelector.wrap(table, ::fakeCompare)
    }

    //endregion

    given("a route selector using a table with no entries") {

        val selector = routeSelector(RoutingTable.empty(invalidRoute()))

        it("selects an invalid route") {
            assertThat(selector.getSelectedRoute(), Is(invalidRoute()))
        }

        it("selects a null neighbor") {
            assertThat(selector.getSelectedNeighbor(), Is(nullValue()))
        }

        on("updating the route of some neighbor with ID 1 to route with preference 10") {

            val updated = selector.update(node(1), route(preference = 10))

            it("selects route with preference 10") {
                assertThat(selector.getSelectedRoute(), Is(route(preference = 10)))
            }

            it("selects neighbor 1") {
                assertThat(selector.getSelectedNeighbor(), Is(node(1)))
            }

            it("indicates the selected route/neighbor was updated") {
                assertThat(updated, Is(true))
            }
        }
    }

    given("a route selector using a table containing a route with preference 10 via a neighbor with ID 1") {

        val selector = routeSelector(RoutingTable.of(invalidRoute(),
                route(preference = 10) via node(1)
        ))

        it("selects route with preference 10") {
            assertThat(selector.getSelectedRoute(), Is(route(preference = 10)))
        }

        it("selects neighbor with ID 1") {
            assertThat(selector.getSelectedNeighbor(), Is(node(1)))
        }

        on("updating the route neighbor with ID 1 to route with preference 15") {

            val updated = selector.update(node(1), route(preference = 15))

            it("selects route with preference 15") {
                assertThat(selector.getSelectedRoute(), Is(route(preference = 15)))
            }

            it("selects neighbor with ID 1") {
                assertThat(selector.getSelectedNeighbor(), Is(node(1)))
            }

            it("indicates the selected route/neighbor was updated") {
                assertThat(updated, Is(true))
            }
        }

        on("updating the route of neighbor with ID 1 to invalid route") {

            val updated = selector.update(node(1), invalidRoute())

            it("selects route invalid route") {
                assertThat(selector.getSelectedRoute(), Is(invalidRoute()))
            }

            it("selects null neighbor") {
                assertThat(selector.getSelectedNeighbor(), Is(nullValue()))
            }

            it("indicates the selected route/neighbor was updated") {
                assertThat(updated, Is(true))
            }
        }

        on("updating the route of neighbor with ID 1 to invalid route again") {

            val updated = selector.update(node(1), invalidRoute())

            it("selects route invalid route") {
                assertThat(selector.getSelectedRoute(), Is(invalidRoute()))
            }

            it("selects null neighbor") {
                assertThat(selector.getSelectedNeighbor(), Is(nullValue()))
            }

            it("indicates the selected route/neighbor was NOT updated") {
                assertThat(updated, Is(false))
            }
        }

    }

    given("a route selector using a table containing invalid routes via neighbors 1 and 2") {

        val selector = routeSelector(RoutingTable.of(invalidRoute(),
                invalidRoute() via node(1),
                invalidRoute() via node(2)
        ))

        it("selects an invalid route") {
            assertThat(selector.getSelectedRoute(), Is(invalidRoute()))
        }

        it("selects a neighbor that is null") {
            assertThat(selector.getSelectedNeighbor(), Is(nullValue()))
        }

        on("updating the route of neighbor 1 to route with preference 10") {

            val updated = selector.update(node(1), route(preference = 10))

            it("selects route with preference 10") {
                assertThat(selector.getSelectedRoute(), Is(route(preference = 10)))
            }

            it("selects neighbor 1") {
                assertThat(selector.getSelectedNeighbor(), Is(node(1)))
            }

            it("indicates the selected route/neighbor was updated") {
                assertThat(updated, Is(true))
            }

        }

        on("updating the route of neighbor 1 to route with preference 5") {

            val updated = selector.update(node(1), route(preference = 5))

            it("selects route with preference 5") {
                assertThat(selector.getSelectedRoute(), Is(route(preference = 5)))
            }

            it("selects neighbor 1") {
                assertThat(selector.getSelectedNeighbor(), Is(node(1)))
            }

            it("indicates the selected route/neighbor was updated") {
                assertThat(updated, Is(true))
            }

        }

        on("updating the route of neighbor 2 to route with preference 15") {

            val updated = selector.update(node(2), route(preference = 15))

            it("selects route with preference 15") {
                assertThat(selector.getSelectedRoute(), Is(route(preference = 15)))
            }

            it("selects neighbor 2") {
                assertThat(selector.getSelectedNeighbor(), Is(node(2)))
            }

            it("indicates the selected route/neighbor was updated") {
                assertThat(updated, Is(true))
            }

        }

        on("updating the route of neighbor 2 to route with preference 1") {

            val updated = selector.update(node(2), route(preference = 1))

            it("selects route with preference 5") {
                assertThat(selector.getSelectedRoute(), Is(route(preference = 5)))
            }

            it("selects neighbor 1") {
                assertThat(selector.getSelectedNeighbor(), Is(node(1)))
            }

            it("indicates the selected route/neighbor was updated") {
                assertThat(updated, Is(true))
            }

        }

        on("updating the route of neighbor 2 to route with preference 3") {

            val updated = selector.update(node(2), route(preference = 3))

            it("selects route with preference 5") {
                assertThat(selector.getSelectedRoute(), Is(route(preference = 5)))
            }

            it("selects neighbor 1") {
                assertThat(selector.getSelectedNeighbor(), Is(node(1)))
            }

            it("indicates the selected route/neighbor was NOT updated") {
                assertThat(updated, Is(false))
            }

        }
    }

    given("a route selector using a table containing valid routes via neighbors 1 and 2 and selecting route via 1") {

        val selector = routeSelector(RoutingTable.of(invalidRoute(),
                route(preference = 10) via node(1),
                route(preference = 5) via node(2)
        ))

        on("disabling neighbor 1") {

            val updated = selector.disable(node(1))

            it("indicates the selected route/neighbor was updated") {
                assertThat(updated, Is(true))
            }

            it("selects route via neighbor 2") {
                assertThat(selector.getSelectedRoute(), Is(route(preference = 5)))
            }

            it("selects neighbor 2") {
                assertThat(selector.getSelectedNeighbor(), Is(node(2)))
            }

        }

        on("updating route via neighbor 1 to a route with preference 15") {

            val updated = selector.update(node(1), route(preference = 15))

            it("indicates the selected route/neighbor was NOT updated") {
                assertThat(updated, Is(false))
            }

            it("selects route via neighbor 2") {
                assertThat(selector.getSelectedRoute(), Is(route(preference = 5)))
            }

            it("selects neighbor 2") {
                assertThat(selector.getSelectedNeighbor(), Is(node(2)))
            }

        }

        on("enabling neighbor 1") {

            val updated = selector.enable(node(1))

            it("indicates the selected route/neighbor was updated") {
                assertThat(updated, Is(true))
            }

            it("selects route with preference 15") {
                assertThat(selector.getSelectedRoute(), Is(route(preference = 15)))
            }

            it("selects neighbor 1") {
                assertThat(selector.getSelectedNeighbor(), Is(node(1)))
            }

        }

        on("disabling neighbor 2") {

            val updated = selector.disable(node(2))

            it("indicates the selected route/neighbor was NOT updated") {
                assertThat(updated, Is(false))
            }

            it("selects route with preference 15") {
                assertThat(selector.getSelectedRoute(), Is(route(preference = 15)))
            }

            it("selects neighbor 1") {
                assertThat(selector.getSelectedNeighbor(), Is(node(1)))
            }

        }

        on("enabling neighbor 2") {

            val updated = selector.enable(node(2))

            it("indicates the selected route/neighbor was NOT updated") {
                assertThat(updated, Is(false))
            }

            it("selects route with preference 15") {
                assertThat(selector.getSelectedRoute(), Is(route(preference = 15)))
            }

            it("selects neighbor 1") {
                assertThat(selector.getSelectedNeighbor(), Is(node(1)))
            }

        }

        on("disabling neighbors 1 and 2") {

            selector.disable(node(1))
            selector.disable(node(2))

            it("selects invalid route") {
                assertThat(selector.getSelectedRoute(), Is(invalidRoute()))
            }

            it("selects null neighbor") {
                assertThat(selector.getSelectedNeighbor(), Is(nullValue()))
            }

        }

    }

    given("a route selector wrapping a table with valid routes via neighbors 1 and 2 is cleared") {

        val selector = routeSelector(RoutingTable.of(invalidRoute(),
                route(preference = 10) via node(1),
                route(preference = 5) via node(2)
        ))

        on("clearing selector") {

            selector.clear()

            it("selects an invalid route") {
                assertThat(selector.getSelectedRoute(), Is(invalidRoute()))
            }

            it("selects a null neighbor") {
                assertThat(selector.getSelectedNeighbor(), Is(nullValue()))
            }
        }

        on("updating neighbor 2 to route with preference 8") {

            selector.update(node(2), route(preference = 8))

            it("selects a route with preference 8") {
                assertThat(selector.getSelectedRoute(), Is(route(preference = 8)))
            }

            it("selects neighbor 2") {
                assertThat(selector.getSelectedNeighbor(), Is(node(2)))
            }
        }

        on("updating neighbor 2 to route with preference 15") {

            selector.update(node(2), route(preference = 15))

            it("selects a route with preference 15") {
                assertThat(selector.getSelectedRoute(), Is(route(preference = 15)))
            }

            it("selects neighbor 2") {
                assertThat(selector.getSelectedNeighbor(), Is(node(2)))
            }
        }

        on("updating neighbor 2 to route with preference 5") {
            // this will for the selector to reselect: if the table was not cleared it will select route(10) via node 1

            selector.update(node(2), route(preference = 5))

            it("selects a route with preference 5") {
                assertThat(selector.getSelectedRoute(), Is(route(preference = 5)))
            }

            it("selects neighbor 2") {
                assertThat(selector.getSelectedNeighbor(), Is(node(2)))
            }
        }
    }

    context("a route selector wrapping a table with 4 entries and neighbors 1 and 3 are disabled") {

        given("all nodes have valid routes and the most preferred route is via node 3") {

            val selector = routeSelector(RoutingTable.of(invalidRoute(),
                    route(preference = 15) via node(1),
                    route(preference = 5) via node(2),
                    route(preference = 30) via node(3),
                    route(preference = 10) via node(4)
            ))

            selector.disable(node(1))
            selector.disable(node(3))

            on("enabling all neighbors") {

                val updated = selector.enableAll()

                it("selects a route with preference 30") {
                    assertThat(selector.getSelectedRoute(), Is(route(preference = 30)))
                }

                it("selects neighbor 3") {
                    assertThat(selector.getSelectedNeighbor(), Is(node(3)))
                }

                it("indicates the selected route/neighbor was updated") {
                    assertThat(updated, Is(true))
                }
            }
        }

        given("all nodes have valid routes and the most preferred route is via node 2") {

            val selector = routeSelector(RoutingTable.of(invalidRoute(),
                    route(preference = 15) via node(1),
                    route(preference = 50) via node(2),
                    route(preference = 30) via node(3),
                    route(preference = 10) via node(4)
            ))

            selector.disable(node(1))
            selector.disable(node(3))

            on("enabling all neighbors") {

                val updated = selector.enableAll()

                it("selects a route with preference 50") {
                    assertThat(selector.getSelectedRoute(), Is(route(preference = 50)))
                }

                it("selects neighbor 2") {
                    assertThat(selector.getSelectedNeighbor(), Is(node(2)))
                }

                it("indicates the selected route/neighbor was NOT updated") {
                    assertThat(updated, Is(false))
                }
            }
        }
    }

    })