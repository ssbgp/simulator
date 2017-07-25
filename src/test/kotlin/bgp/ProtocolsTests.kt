package bgp

import core.routing.*
import core.simulator.Scheduler
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.on
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.*
import testing.via

/**
 * Created on 21-07-2017

 * @author David Fialho
 */
object BGPProtocolTests : Spek({

    given("a node with ID 1 running BGP protocol learns a route from node with ID 2") {

        val protocol = BGPProtocol()
        val node = BGPNode.with(id = 1)
        val sender = BGPNode.with(id = 2)

        on("imported route is invalid") {

            val learnedRoute = protocol.learn(node, sender, invalid())

            it("learns an invalid route") {
                assertThat(learnedRoute, `is`(invalid()))
            }

            it("indicates the selected route was not updated") {
                assertThat(protocol.wasSelectedRouteUpdated, `is`(false))
            }
        }

        on("imported route is valid with LOCAL-PREF 10 and AS-PATH [3, 2]") {

            val importedRoute = route(localPref = 10, asPath = pathOf(BGPNode.with(id = 3), BGPNode.with(id = 2)))
            val learnedRoute = protocol.learn(node, sender, importedRoute)

            it("learns the imported route") {
                assertThat(learnedRoute, `is`(importedRoute))
            }

            it("indicates the selected route was not updated") {
                assertThat(protocol.wasSelectedRouteUpdated, `is`(false))
            }
        }

        on("imported route is valid with LOCAL-PREF 10 and AS-PATH [3, 1, 2]") {

            val importedRoute = route(
                    localPref = 10,
                    asPath = pathOf(BGPNode.with(id = 3), BGPNode.with(id = 1), BGPNode.with(id = 2))
            )

            val learnedRoute = protocol.learn(node, sender, importedRoute)

            it("learns an invalid route") {
                assertThat(learnedRoute, `is`(invalid()))
            }

            it("indicates the selected route was not updated") {
                assertThat(protocol.wasSelectedRouteUpdated, `is`(false))
            }
        }
    }

    given("a node that has never exported a route") {

        val node = mock(BGPNode::class.java)
        val protocol = BGPProtocol(mrai = 10)
        val selectedRoute = route(localPref = 100, asPath = emptyPath())

        beforeEachTest {
            // Reset the number of calls to export
            reset(node)

            // Force the selected route
            @Suppress("UNCHECKED_CAST")
            val table = mock(RouteSelector::class.java) as RouteSelector<BGPNode, BGPRoute>
            `when`(table.getSelectedRoute()).thenReturn(selectedRoute)
            `when`(node.routingTable).thenReturn(table)
        }

        on("calling export") {

            protocol.export(node)

            it("exports the selected route") {
                verify(node, times(1)).export(selectedRoute)
            }

            it("starts the MRAI timer") {
                assertThat(protocol.mraiTimer.expired, `is`(false))
            }

        }

        on("calling export again before the timer expires") {

            protocol.export(node)

            it("does not export the route") {
                verify(node, never()).export(route = any())
            }

            it("indicates the MRAI timer has not expired") {
                assertThat(protocol.mraiTimer.expired, `is`(false))
            }
        }

        on("MRAI timer expires") {

            // emulates the expired event being taken from the scheduler
            protocol.mraiTimer.onExpired()

            it("exports the selected route to the node's neighbors") {
                verify(node, times(1)).export(selectedRoute)
            }

            it("starts the MRAI timer again") {
                assertThat(protocol.mraiTimer.expired, `is`(false))
            }
        }
    }

    given("an MRAI value of 0") {

        val node = mock(BGPNode::class.java)
        val protocol = BGPProtocol(mrai = 0)
        val selectedRoute = route(localPref = 100, asPath = emptyPath())

        beforeEachTest {
            // Reset the number of calls to export
            reset(node)

            // Force the selected route
            @Suppress("UNCHECKED_CAST")
            val table = mock(RouteSelector::class.java) as RouteSelector<BGPNode, BGPRoute>
            `when`(table.getSelectedRoute()).thenReturn(selectedRoute)
            `when`(node.routingTable).thenReturn(table)
        }

        on("calling export 3 times") {

            protocol.export(node)
            protocol.export(node)
            protocol.export(node)

            it("exports the route 3 times") {
                verify(node, times(3)).export(selectedRoute)
            }

            it("MRAI timer is never started") {
                assertThat(protocol.mraiTimer.expired, `is`(true))
            }
        }
    }

    // TODO test process!!
})


object SSBGPProtocolTests : Spek({

    context("node detects a routing loop") {

        val protocol = SSBGPProtocol(mrai = 0)
        val sender = BGPNode.with(id = 10)
        val loopingPath = pathOf(BGPNode.with(1))
        val node = mockNodeWith(id = 1)

        given("node was selecting a route via another neighbor") {

            beforeEachTest {
                resetRoutingTable(node, table = RoutingTable.of(BGPRoute.invalid(),
                        // Alternative route
                        BGPRoute.with(localPref = 100, asPath = emptyPath()) via BGPNode.with(2)
                ))
            }

            on("the LOCAL-PREF of the imported route is higher than the alternative route's") {

                val importedRoute = BGPRoute.with(localPref = 200, asPath = loopingPath)

                protocol.onLoopDetected(node, sender, importedRoute)

                it("disables the neighbor which sent the route") {
                    verify(node.routingTable, times(1)).disable(sender)
                }
            }

            on("the LOCAL-PREF of the imported route is equal to the alternative route's") {

                val importedRoute = BGPRoute.with(localPref = 100, asPath = loopingPath)

                protocol.onLoopDetected(node, sender, importedRoute)

                it("does not disabled the neighbor who sent the route") {
                    verify(node.routingTable, never()).disable(any())
                }
            }

            on("the LOCAL-PREF of the imported route is lower than the alternative route's") {

                val importedRoute = BGPRoute.with(localPref = 50, asPath = loopingPath)

                protocol.onLoopDetected(node, sender, importedRoute)

                it("does not disabled the neighbor who sent the route") {
                    verify(node.routingTable, never()).disable(any())
                }
            }
        }

        given("node was selecting a route via the neighbor that sent the looping route and has no alternative") {

            beforeEachTest {
                resetRoutingTable(node, table = RoutingTable.of(BGPRoute.invalid(),
                        BGPRoute.with(localPref = 100, asPath = emptyPath()) via sender
                ))
            }

            on("the LOCAL-PREF of the imported route is higher than its current route via the sender") {

                val importedRoute = BGPRoute.with(localPref = 200, asPath = loopingPath)

                protocol.onLoopDetected(node, sender, importedRoute)

                it("disables the neighbor which sent the route") {
                    verify(node.routingTable, times(1)).disable(sender)
                }
            }

            on("the LOCAL-PREF of the imported route is equal to its current route via the sender") {

                val importedRoute = BGPRoute.with(localPref = 100, asPath = loopingPath)

                protocol.onLoopDetected(node, sender, importedRoute)

                it("disables the neighbor which sent the route") {
                    verify(node.routingTable, times(1)).disable(sender)
                }
            }

            on("the LOCAL-PREF of the imported route is lower than its current route via the sender") {

                val importedRoute = BGPRoute.with(localPref = 50, asPath = loopingPath)

                protocol.onLoopDetected(node, sender, importedRoute)

                it("disables the neighbor which sent the route") {
                    verify(node.routingTable, times(1)).disable(sender)
                }
            }
        }

        given("node was selecting a route via the neighbor that sent the looping route and has alternative") {

            beforeEachTest {
                resetRoutingTable(node, table = RoutingTable.of(BGPRoute.invalid(),
                        BGPRoute.with(localPref = 100, asPath = emptyPath()) via sender,
                        BGPRoute.with(localPref = 50, asPath = emptyPath()) via BGPNode.with(2)
                ))
            }

            on("the LOCAL-PREF of the imported route is higher than the alternative route's") {

                val importedRoute = BGPRoute.with(localPref = 200, asPath = loopingPath)

                protocol.onLoopDetected(node, sender, importedRoute)

                it("disables the neighbor which sent the route") {
                    verify(node.routingTable, times(1)).disable(sender)
                }
            }

            on("the LOCAL-PREF of the imported route is equal to the alternative route's") {

                val importedRoute = BGPRoute.with(localPref = 50, asPath = loopingPath)

                protocol.onLoopDetected(node, sender, importedRoute)

                it("does not disabled the neighbor who sent the route") {
                    verify(node.routingTable, never()).disable(any())
                }
            }

            on("the LOCAL-PREF of the imported route is lower than the alternative route's") {

                val importedRoute = BGPRoute.with(localPref = 25, asPath = loopingPath)

                protocol.onLoopDetected(node, sender, importedRoute)

                it("does not disabled the neighbor who sent the route") {
                    verify(node.routingTable, never()).disable(any())
                }
            }
        }
    }

})

//region Helper methods

/**
 * Shorter way to create a valid BGP route.
 */
fun route(localPref: Int, asPath: Path<BGPNode>) = BGPRoute.with(localPref, asPath)

/**
 * Shorter way to create an invalid BGP route.
 */
fun invalid() = BGPRoute.invalid()

/**
 * Work around over Mockito.any() to avoid error: 'IllegalStateException: Mockito.any() must not be null'
 */
fun <T> any(): T {
    Mockito.any<T>()
    @Suppress("UNCHECKED_CAST")
    return null as T
}

/**
 * Returns a mocked node with the specified ID and routing table. If no routing table is provided it uses an empty
 * routing table.
 */
fun mockNodeWith(id: NodeID, table: RoutingTable<BGPNode, BGPRoute> = RoutingTable.empty(BGPRoute.invalid())): BGPNode {

    val node = mock(BGPNode::class.java)
    `when`(node.id).thenReturn(id)

    val selector = spy(RouteSelector.wrap(table, ::bgpRouteCompare))
    `when`(node.routingTable).thenReturn(selector)

    return node
}

fun resetRoutingTable(node: BGPNode, table: RoutingTable<BGPNode, BGPRoute> = RoutingTable.empty(BGPRoute.invalid())) {

    val selector = spy(RouteSelector.wrap(table, ::bgpRouteCompare))
    `when`(node.routingTable).thenReturn(selector)
}

//endregion