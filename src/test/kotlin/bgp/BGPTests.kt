package bgp

import com.nhaarman.mockito_kotlin.*
import core.routing.*
import core.simulator.Engine
import core.simulator.Time
import org.hamcrest.MatcherAssert.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import testing.`when`
import testing.bgp.BGPNode
import org.hamcrest.Matchers.`is` as Is

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
object BGPTests : Spek({

    context("node with ID 1 imports a new route from neighbor with ID 2 with MRAI disabled") {

        val protocol = BGP(routingTable = spy(RoutingTable.empty(BGPRoute.invalid())))
        val node = spy(Node(1, protocol))
        val neighbor = BGPNode(id = 2)

        // Make sure the scheduler is kept clean for the next tests
        afterGroup { Engine.scheduler.reset() }

        // Reset spies
        afterEachTest {
            reset(node)
            reset(protocol.routingTable.table)
        }

        given("a node with empty protocol state and with MRAI timer disabled") {

            beforeEachTest {
                // Keep the protocol state clean after each test
                protocol.reset()
            }

            `when`("node imports an invalid route") {

                protocol.process(node, neighbor, importedRoute = BGPRoute.invalid())

                it("does not send any route") {
                    verify(node, never()).send(any())
                }

                it("stores an invalid route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, times(1)).set(neighbor, BGPRoute.invalid())
                }

                it("does not select any route") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.invalid()))
                }
            }

            `when`("node imports route(10, [0, 2])") {

                val importedRoute = BGPRoute.with(localPref = 10, asPath = pathOf(BGPNode(0), neighbor))
                protocol.process(node, neighbor, importedRoute)

                it("exports the newly imported route") {
                    verify(node, times(1)).send(importedRoute)
                }

                it("stores imported route as candidate route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, times(1)).set(neighbor, importedRoute)
                }

                it("selects the newly imported route") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(importedRoute))
                }
            }

            `when`("node imports route(10, [0, 1, 2])") {

                val importedRoute = BGPRoute.with(localPref = 10, asPath = pathOf(BGPNode(0), node, neighbor))
                protocol.process(node, neighbor, importedRoute)

                it("does not send any route") {
                    verify(node, never()).send(any())
                }

                it("stores an invalid route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, times(1)).set(neighbor, BGPRoute.invalid())
                }

                it("does not select any route") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.invalid()))
                }
            }
        }

        given("node selects route(10, [0, 2]) via node 2 and with no alternative") {

            beforeEachTest {
                // Keep the protocol state clean after each test
                protocol.reset()
                protocol.process(node, neighbor, BGPRoute.with(10, pathOf(BGPNode(0), neighbor)))
            }

            `when`("node imports an invalid route") {

                protocol.process(node, neighbor, importedRoute = BGPRoute.invalid())

                it("exports an invalid route") {
                    verify(node, times(1)).send(BGPRoute.invalid())
                }

                it("stores an invalid route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, times(1)).set(neighbor, BGPRoute.invalid())
                }

                it("does not select any route") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.invalid()))
                }
            }

            `when`("node imports route(20, [0, 2])") {

                val importedRoute = BGPRoute.with(localPref = 20, asPath = pathOf(BGPNode(0), neighbor))
                protocol.process(node, neighbor, importedRoute)

                it("exports the newly imported route") {
                    verify(node, times(1)).send(importedRoute)
                }

                it("stores imported route as candidate route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, times(1)).set(neighbor, importedRoute)
                }

                it("selects the newly imported route") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(importedRoute))
                }
            }

            `when`("node imports route(5, [0, 2])") {

                val importedRoute = BGPRoute.with(localPref = 5, asPath = pathOf(BGPNode(0), neighbor))
                protocol.process(node, neighbor, importedRoute)

                it("exports the newly imported route") {
                    verify(node, times(1)).send(importedRoute)
                }

                it("stores imported route as candidate route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, times(1)).set(neighbor, importedRoute)
                }

                it("selects the newly imported route") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(importedRoute))
                }
            }

            `when`("node imports route(20, [0, 1, 2])") {

                val importedRoute = BGPRoute.with(localPref = 20, asPath = pathOf(BGPNode(0), node, neighbor))
                protocol.process(node, neighbor, importedRoute)

                it("exports an invalid route") {
                    verify(node, times(1)).send(BGPRoute.invalid())
                }

                it("stores an invalid route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, times(1)).set(neighbor, BGPRoute.invalid())
                }

                it("does not select any route") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.invalid()))
                }
            }
        }

        given("node selects route(10, [0, 3]) via node 3 and with no alternative") {

            beforeEachTest {
                // Keep the protocol state clean after each test
                protocol.reset()
                val neighbor3 = BGPNode(3)
                protocol.routingTable.update(neighbor3, BGPRoute.with(10, pathOf(BGPNode(0), neighbor3)))
            }

            `when`("node imports an invalid route") {

                protocol.process(node, neighbor, importedRoute = BGPRoute.invalid())

                it("does not send any route") {
                    verify(node, never()).send(any())
                }

                it("stores an invalid route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, times(1)).set(neighbor, BGPRoute.invalid())
                }

                it("keeps selecting route(10, , [0, 3])") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.with(10, pathOf(BGPNode(0), BGPNode(3)))))
                }
            }

            `when`("node imports route(20, [0, 2])") {

                val importedRoute = BGPRoute.with(localPref = 20, asPath = pathOf(BGPNode(0), neighbor))
                protocol.process(node, neighbor, importedRoute)

                it("exports the newly imported route") {
                    verify(node, times(1)).send(importedRoute)
                }

                it("stores imported route as candidate route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, times(1)).set(neighbor, importedRoute)
                }

                it("selects the newly imported route") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(importedRoute))
                }
            }

            `when`("node imports route(5, [0, 2])") {

                val importedRoute = BGPRoute.with(localPref = 5, asPath = pathOf(BGPNode(0), neighbor))
                protocol.process(node, neighbor, importedRoute)

                it("does not send any route") {
                    verify(node, never()).send(any())
                }

                it("stores imported route as candidate route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, times(1)).set(neighbor, importedRoute)
                }

                it("keeps selecting route(10, , [0, 3])") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.with(10, pathOf(BGPNode(0), BGPNode(3)))))
                }
            }

            `when`("node imports route(20, [0, 1, 2])") {

                val importedRoute = BGPRoute.with(localPref = 20, asPath = pathOf(BGPNode(0), node, neighbor))
                protocol.process(node, neighbor, importedRoute)

                it("does not send any route") {
                    verify(node, never()).send(any())
                }

                it("stores an invalid route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, times(1)).set(neighbor, BGPRoute.invalid())
                }

                it("keeps selecting route(10, , [0, 3])") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.with(10, pathOf(BGPNode(0), BGPNode(3)))))
                }
            }
        }

        given("node selects route(10, [0, 2]) via node 2 and alternative route(5, [0, 3]) via node 3") {

            val neighbor3 = BGPNode(3)

            beforeEachTest {
                protocol.reset()

                // Alternative route
                protocol.routingTable.update(neighbor3, BGPRoute.with(5, pathOf(BGPNode(0), neighbor3)))
                // Selected route
                protocol.routingTable.update(neighbor, BGPRoute.with(10, pathOf(BGPNode(0), neighbor)))
            }

            `when`("node imports an invalid route") {

                protocol.process(node, neighbor, importedRoute = BGPRoute.invalid())

                it("exports route(5, [0, 3])") {
                    verify(node, times(1)).send(BGPRoute.with(5, pathOf(BGPNode(0), neighbor3)))
                }

                it("stores an invalid route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, times(1)).set(neighbor, BGPRoute.invalid())
                }

                it("selects route route(5, [0, 3])") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.with(5, pathOf(BGPNode(0), neighbor3))))
                }
            }

            `when`("node imports route(20, [0, 2])") {

                val importedRoute = BGPRoute.with(localPref = 20, asPath = pathOf(BGPNode(0), neighbor))
                protocol.process(node, neighbor, importedRoute)

                it("exports the newly imported route") {
                    verify(node, times(1)).send(importedRoute)
                }

                it("stores imported route as candidate route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, times(1)).set(neighbor, importedRoute)
                }

                it("selects the newly imported route") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(importedRoute))
                }
            }

            `when`("node imports route(7, [0, 2])") {

                val importedRoute = BGPRoute.with(localPref = 7, asPath = pathOf(BGPNode(0), neighbor))
                protocol.process(node, neighbor, importedRoute)

                it("exports the newly imported route") {
                    verify(node, times(1)).send(importedRoute)
                }

                it("stores imported route as candidate route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, times(1)).set(neighbor, importedRoute)
                }

                it("selects the newly imported route") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(importedRoute))
                }
            }

            `when`("node imports route(20, [0, 1, 2])") {

                val importedRoute = BGPRoute.with(localPref = 20, asPath = pathOf(BGPNode(0), node, neighbor))
                protocol.process(node, neighbor, importedRoute)

                it("exports route(5, [0, 3])") {
                    verify(node, times(1)).send(BGPRoute.with(5, pathOf(BGPNode(0), neighbor3)))
                }

                it("stores an invalid route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, times(1)).set(neighbor, BGPRoute.invalid())
                }

                it("selects route route(5, [0, 3])") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.with(5, pathOf(BGPNode(0), neighbor3))))
                }
            }
        }

        given("node selects route(10, [0, 3]) via node 3 and alternative route(5, [0, 2]) via node 2") {

            val neighbor3 = BGPNode(3)

            beforeEachTest {
                protocol.reset()

                // Selected route
                protocol.routingTable.update(neighbor3, BGPRoute.with(10, pathOf(BGPNode(0), neighbor3)))
                // Alternative route
                protocol.routingTable.update(neighbor, BGPRoute.with(5, pathOf(BGPNode(0), neighbor)))
            }

            `when`("node imports an invalid route") {

                protocol.process(node, neighbor, importedRoute = BGPRoute.invalid())

                it("does not send any route") {
                    verify(node, never()).send(any())
                }

                it("stores an invalid route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, times(1)).set(neighbor, BGPRoute.invalid())
                }

                it("selects route route(10, [0, 3])") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.with(10, pathOf(BGPNode(0), neighbor3))))
                }
            }

            `when`("node imports route(20, [0, 2])") {

                val importedRoute = BGPRoute.with(localPref = 20, asPath = pathOf(BGPNode(0), neighbor))
                protocol.process(node, neighbor, importedRoute)

                it("exports the newly imported route") {
                    verify(node, times(1)).send(importedRoute)
                }

                it("stores imported route as candidate route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, times(1)).set(neighbor, importedRoute)
                }

                it("selects the newly imported route") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(importedRoute))
                }
            }

            `when`("node imports route(7, [0, 2])") {

                val importedRoute = BGPRoute.with(localPref = 7, asPath = pathOf(BGPNode(0), neighbor))
                protocol.process(node, neighbor, importedRoute)

                it("does not send any route") {
                    verify(node, never()).send(any())
                }

                it("stores imported route as candidate route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, times(1)).set(neighbor, importedRoute)
                }

                it("selects route route(10, [0, 3])") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.with(10, pathOf(BGPNode(0), neighbor3))))
                }
            }

            `when`("node imports route(20, [0, 1, 2])") {

                val importedRoute = BGPRoute.with(localPref = 20, asPath = pathOf(BGPNode(0), node, neighbor))
                protocol.process(node, neighbor, importedRoute)

                it("does not send any route") {
                    verify(node, never()).send(any())
                }

                it("stores an invalid route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, times(1)).set(neighbor, BGPRoute.invalid())
                }

                it("selects route route(10, [0, 3])") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.with(10, pathOf(BGPNode(0), neighbor3))))
                }
            }
        }

    }

    fun BGP(mrai: Time = 0) = BGP(mrai, routingTable = spy(RoutingTable.empty(BGPRoute.invalid())))
    fun SpyBGPNode(protocol: Protocol<BGPRoute>) = spy(Node(1, protocol))

    context("node exports a new route with MRAI timer enabled") {

        val neighbor = BGPNode(2)
        val exportedRoute = BGPRoute.with(localPref = 10, asPath = pathOf(BGPNode(0), neighbor))

        // Make sure the scheduler is kept clean for the next tests
        afterGroup { Engine.scheduler.reset() }

        given("node never exported a route") {

            val protocol = BGP(mrai = 10)
            val node = SpyBGPNode(protocol)

            protocol.process(node, neighbor, exportedRoute)

            it("sends the route to neighbors immediately") {
                verify(node, times(1)).send(exportedRoute)
            }

            it("starts a new MRAI timer") {
                assertThat(protocol.mraiTimer.expired,
                        Is(false))
            }
        }

        given("the MRAI timer has expired after previous send") {

            val protocol = BGP(mrai = 10)
            val node = SpyBGPNode(protocol)

            // Node exports some route that starts the MRAI timer
            protocol.process(node, neighbor, BGPRoute.with(localPref = 10, asPath = emptyPath()))
            // MRAI Timer expires
            protocol.mraiTimer.onExpired()

            // Ensure the node call count is clean
            reset(node)

            // Node has a new route to export
            protocol.process(node, neighbor, exportedRoute)

            it("sends the route to neighbors immediately") {
                verify(node, times(1)).send(exportedRoute)
            }

            it("starts a new MRAI timer") {
                assertThat(protocol.mraiTimer.expired,
                        Is(false))
            }
        }

        given("the MRAI timer is running") {

            val protocol = BGP(mrai = 10)
            val node = SpyBGPNode(protocol)

            // Node exports some route that starts the MRAI timer
            protocol.process(node, neighbor, BGPRoute.with(localPref = 10, asPath = emptyPath()))

            // Ensure the node call count is clean
            reset(node)

            // Node has a new route to export
            protocol.process(node, neighbor, exportedRoute)

            it("does NOT send route to neighbors") {
                verify(node, never()).send(any())
            }

            `when`("the MRAI timer expires") {

                protocol.mraiTimer.onExpired()

                it("sends the newly exported route") {
                    verify(node, times(1)).send(exportedRoute)
                }

                it("does start a new MRAI timer") {
                    assertThat(protocol.mraiTimer.expired,
                            Is(false))
                }
            }
        }
    }

})
