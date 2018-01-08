package bgp

import com.nhaarman.mockito_kotlin.*
import core.routing.Node
import core.routing.Protocol
import core.routing.RoutingTable
import core.routing.pathOf
import core.simulator.Time
import core.simulator.Simulator
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is` as Is
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import testing.`when`
import testing.bgp.BGPNode

/**
 * Created on 28-08-2017.
 *
 * @author David Fialho
 */
object SSBGPTests : Spek({

    fun SSBGP(mrai: Time = 0) = SSBGP(mrai, routingTable = spy(RoutingTable.empty(BGPRoute.invalid())))
    fun SpyBGPNode(protocol: Protocol<BGPRoute>) = spy(Node(1, protocol))

    context("node with ID 1 imports a new route from neighbor with ID 2 with MRAI disabled") {

        val neighbor = BGPNode(id = 2)

        // Make sure the scheduler is kept clean for the next tests
        afterGroup { Simulator.scheduler.reset() }

        given("a node with empty protocol state and with MRAI timer disabled") {

            val protocol = SSBGP()
            val node = SpyBGPNode(protocol)
            beforeEachTest { protocol.reset() }

            `when`("node imports looping route(10, [0, 1, 2])") {

                protocol.process(node, neighbor, importedRoute = BGPRoute.with(10, pathOf(BGPNode(0), node, neighbor)))

                it("stores an invalid route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, atLeastOnce()).set(neighbor, BGPRoute.invalid())
                }

                it("does not select any route") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.invalid()))
                }

                it("disables neighbor 2") {
                    assertThat(protocol.routingTable.table.isEnabled(neighbor),
                            Is(false))
                }
            }
        }

        given("node selects route(10, [0, 2]) via node 2 and with no alternative") {

            val protocol = SSBGP()
            val node = SpyBGPNode(protocol)

            beforeEachTest {
                protocol.reset()
                protocol.routingTable.update(neighbor, BGPRoute.with(10, pathOf(BGPNode(0), neighbor)))
                reset(protocol.routingTable.table)
            }

            `when`("node imports looping route(10, [0, 1, 2])") {

                protocol.process(node, neighbor, importedRoute = BGPRoute.with(10, pathOf(BGPNode(0), node, neighbor)))

                it("stores an invalid route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, atLeastOnce()).set(neighbor, BGPRoute.invalid())
                }

                it("does not select any route") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.invalid()))
                }

                it("disables neighbor 2") {
                    assertThat(protocol.routingTable.table.isEnabled(neighbor),
                            Is(false))
                }
            }

            `when`("node imports looping route(20, [0, 1, 2])") {

                protocol.process(node, neighbor, importedRoute = BGPRoute.with(20, pathOf(BGPNode(0), node, neighbor)))

                it("stores an invalid route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, atLeastOnce()).set(neighbor, BGPRoute.invalid())
                }

                it("does not select any route") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.invalid()))
                }

                it("disables neighbor 2") {
                    assertThat(protocol.routingTable.table.isEnabled(neighbor),
                            Is(false))
                }
            }
        }

        given("node selects route(10, [0, 3]) via node 3 and with no alternative") {

            val protocol = SSBGP()
            val node = SpyBGPNode(protocol)
            val neighbor3 = BGPNode(3)

            beforeEachTest {
                protocol.reset()
                protocol.routingTable.update(neighbor3, BGPRoute.with(10, pathOf(BGPNode(0), neighbor3)))
                reset(protocol.routingTable.table)
            }

            `when`("node imports looping route(20, [0, 1, 2])") {

                protocol.process(node, neighbor, importedRoute = BGPRoute.with(20, pathOf(BGPNode(0), node, neighbor)))

                it("stores an invalid route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, atLeastOnce()).set(neighbor, BGPRoute.invalid())
                }

                it("selects route(10, [0, 3])") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.with(10, pathOf(BGPNode(0), neighbor3))))
                }

                it("disables neighbor 2") {
                    assertThat(protocol.routingTable.table.isEnabled(neighbor),
                            Is(false))
                }
            }

            `when`("node imports looping route(10, [0, 1, 2])") {

                protocol.process(node, neighbor, importedRoute = BGPRoute.with(10, pathOf(BGPNode(0), node, neighbor)))

                it("stores an invalid route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, atLeastOnce()).set(neighbor, BGPRoute.invalid())
                }

                it("selects route(10, [0, 3])") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.with(10, pathOf(BGPNode(0), neighbor3))))
                }

                it("does NOT disable neighbor 2") {
                    assertThat(protocol.routingTable.table.isEnabled(neighbor),
                            Is(true))
                }
            }

            `when`("node imports looping route(5, [0, 1, 2])") {

                protocol.process(node, neighbor, importedRoute = BGPRoute.with(5, pathOf(BGPNode(0), node, neighbor)))

                it("stores an invalid route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, atLeastOnce()).set(neighbor, BGPRoute.invalid())
                }

                it("selects route(10, [0, 3])") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.with(10, pathOf(BGPNode(0), neighbor3))))
                }

                it("does NOT disable neighbor 2") {
                    assertThat(protocol.routingTable.table.isEnabled(neighbor),
                            Is(true))
                }
            }

            `when`("node imports looping route(20, [0, 4, 1, 2])") {

                val importedRoute = BGPRoute.with(20, pathOf(BGPNode(0), BGPNode(4), node, neighbor))
                protocol.process(node, neighbor, importedRoute)

                it("stores an invalid route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, atLeastOnce()).set(neighbor, BGPRoute.invalid())
                }

                it("selects route(10, [0, 3])") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.with(10, pathOf(BGPNode(0), neighbor3))))
                }

                it("disables neighbor 2") {
                    assertThat(protocol.routingTable.table.isEnabled(neighbor),
                            Is(false))
                }
            }
        }

        given("node selects route(10, [0, 3]) via node 3, with no alternative, and neighbor 2 is disabled") {

            val protocol = SSBGP()
            val node = SpyBGPNode(protocol)
            val neighbor3 = BGPNode(3)

            beforeEachTest {
                protocol.reset()
                protocol.routingTable.update(neighbor3, BGPRoute.with(10, pathOf(BGPNode(0), neighbor3)))
                protocol.disableNeighbor(neighbor)
                reset(protocol.routingTable.table)
            }

            `when`("node imports route(5, [0, 2])") {

                val importedRoute = BGPRoute.with(5, pathOf(BGPNode(0), neighbor))
                protocol.process(node, neighbor, importedRoute)

                it("stores route(5, [0, 2] as candidate route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, atLeastOnce()).set(neighbor, importedRoute)
                }

                it("selects route(10, [0, 3])") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.with(10, pathOf(BGPNode(0), neighbor3))))
                }

                it("neighbor 2 is still disabled") {
                    assertThat(protocol.routingTable.table.isEnabled(neighbor),
                            Is(false))
                }
            }

            `when`("node imports route(20, [0, 2])") {

                val importedRoute = BGPRoute.with(20, pathOf(BGPNode(0), neighbor))
                protocol.process(node, neighbor, importedRoute)

                it("stores route(20, [0, 2] as candidate route via neighbor 2") {
                    @Suppress("ReplaceGetOrSet")
                    verify(protocol.routingTable.table, atLeastOnce()).set(neighbor, importedRoute)
                }

                it("selects route(10, [0, 3])") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.with(10, pathOf(BGPNode(0), neighbor3))))
                }

                it("neighbor 2 is still disabled") {
                    assertThat(protocol.routingTable.table.isEnabled(neighbor),
                            Is(false))
                }
            }
        }
    }

    context("node with ID 1 disables/enables neighbor with ID 2") {

        val neighbor2 = BGPNode(id = 2)
        val neighbor3 = BGPNode(id = 3)

        // Make sure the scheduler is kept clean for the next tests
        afterGroup { Simulator.scheduler.reset() }

        given("node selects route (10, [0, 3]) via node 3 and with alternative route (5, [0, 2]) via node 2") {

            val protocol = SSBGP()

            protocol.routingTable.update(neighbor3, BGPRoute.with(10, pathOf(BGPNode(0), neighbor3)))
            protocol.routingTable.update(neighbor2, BGPRoute.with(5, pathOf(BGPNode(0), neighbor2)))

            `when`("it disables neighbor 2") {

                protocol.disableNeighbor(neighbor2)

                it("selects route (10, [0, 3])") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.with(localPref = 10, asPath = pathOf(BGPNode(0), neighbor3))))
                }

                it("still indicates neighbor 2 is disabled") {
                    assertThat(protocol.routingTable.table.isEnabled(neighbor2),
                            Is(false))
                }
            }

            `when`("it enables back neighbor 2") {

                protocol.enableNeighbor(neighbor2)

                it("selects route (10, [0, 3])") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.with(localPref = 10, asPath = pathOf(BGPNode(0), neighbor3))))
                }

                it("enabled neighbor 2") {
                    assertThat(protocol.routingTable.table.isEnabled(neighbor2),
                            Is(true))
                }
            }
        }

        given("node selects route (10, [0, 2]) via node 2 and with alternative route (5, [0, 3]) via node 3") {

            val protocol = SSBGP()

            protocol.routingTable.update(neighbor2, BGPRoute.with(10, pathOf(BGPNode(0), neighbor2)))
            protocol.routingTable.update(neighbor3, BGPRoute.with(5, pathOf(BGPNode(0), neighbor3)))

            protocol.disableNeighbor(neighbor2)

            `when`("it disables neighbor 2") {

                protocol.disableNeighbor(neighbor2)

                it("selects route (5, [0, 3])") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.with(localPref = 5, asPath = pathOf(BGPNode(0), neighbor3))))
                }

                it("still indicates neighbor 2 is disabled") {
                    assertThat(protocol.routingTable.table.isEnabled(neighbor2),
                            Is(false))
                }
            }

            `when`("it enables back neighbor 2") {

                protocol.enableNeighbor(neighbor2)

                it("selects route (10, [0, 2])") {
                    assertThat(protocol.routingTable.getSelectedRoute(),
                            Is(BGPRoute.with(localPref = 10, asPath = pathOf(BGPNode(0), neighbor2))))
                }

                it("enabled neighbor 2") {
                    assertThat(protocol.routingTable.table.isEnabled(neighbor2),
                            Is(true))
                }
            }
        }
    }
})