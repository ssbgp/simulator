package bgp

import core.routing.RoutingTable
import core.routing.pathOf
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.mockito.Mockito.*
import testing.mock.mockNodeWith
import testing.mock.resetRoutingTable
import testing.via
import testing.mock.any

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
object ISSBGPProtocolTests : Spek({

    context("node detects a routing loop") {

        val protocol = ISSBGPProtocol(mrai = 0)
        val sender = BGPNode.with(id = 10)
        val node = mockNodeWith(id = 1)
        val destination = BGPNode.with(0)

        given("node was selecting a route via another neighbor") {

            beforeEachTest {
                resetRoutingTable(node, table = RoutingTable.of(BGPRoute.invalid(),
                        BGPRoute.with(100, pathOf(destination, BGPNode.with(2))) via BGPNode.with(2)
                ))
            }

            on("the LOCAL-PREF of the imported route is higher than the alternative route's and the subpath matches") {

                val importedRoute = BGPRoute.with(
                        localPref = 200,
                        asPath = pathOf(destination, BGPNode.with(2), node, sender)
                )

                protocol.onLoopDetected(node, sender, importedRoute)

                it("disables the neighbor which sent the route") {
                    verify(node.routingTable, times(1)).disable(sender)
                }
            }

            on("the LOCAL-PREF of the imported route is equal to the alternative route's and the subpath matches") {

                val importedRoute = BGPRoute.with(
                        localPref = 100,
                        asPath = pathOf(destination, BGPNode.with(2), node, sender)
                )

                protocol.onLoopDetected(node, sender, importedRoute)

                it("does not disabled the neighbor who sent the route") {
                    verify(node.routingTable, never()).disable(any())
                }
            }

            on("the LOCAL-PREF of the imported route is lower than the alternative route's and the subpath matches") {

                val importedRoute = BGPRoute.with(
                        localPref = 50,
                        asPath = pathOf(destination, BGPNode.with(2), node, sender)
                )

                protocol.onLoopDetected(node, sender, importedRoute)

                it("does not disabled the neighbor who sent the route") {
                    verify(node.routingTable, never()).disable(any())
                }
            }

            on("the LOCAL-PREF is higher than the alternative route's and the subpath does NOT match") {

                val importedRoute = BGPRoute.with(
                        localPref = 200,
                        asPath = pathOf(destination, BGPNode.with(3), node, sender)
                )

                protocol.onLoopDetected(node, sender, importedRoute)

                it("does not disabled the neighbor who sent the route") {
                    verify(node.routingTable, never()).disable(any())
                }
            }
        }

        given("node was selecting a route via the neighbor that sent the looping route and has alternative") {

            beforeEachTest {
                resetRoutingTable(node, table = RoutingTable.of(BGPRoute.invalid(),
                        BGPRoute.with(100, pathOf(destination, sender)) via sender,
                        BGPRoute.with(50, pathOf(destination, BGPNode.with(2))) via BGPNode.with(2) // alternative route
                ))
            }

            on("the LOCAL-PREF of the imported route is higher than the alternative route's and the subpath matches") {

                val importedRoute = BGPRoute.with(
                        localPref = 200,
                        asPath = pathOf(destination, BGPNode.with(2), node, sender)
                )

                protocol.onLoopDetected(node, sender, importedRoute)

                it("disables the neighbor which sent the route") {
                    verify(node.routingTable, times(1)).disable(sender)
                }
            }

            on("the LOCAL-PREF of the imported route is higher than the alternative route's and lower than previous " +
                    "route via the sender and the subpath matches") {

                val importedRoute = BGPRoute.with(
                        localPref = 75,
                        asPath = pathOf(destination, BGPNode.with(2), node, sender)
                )

                protocol.onLoopDetected(node, sender, importedRoute)

                it("disables the neighbor which sent the route") {
                    verify(node.routingTable, times(1)).disable(sender)
                }
            }

            on("the LOCAL-PREF of the imported route is equal to the alternative route's and the subpath matches") {

                val importedRoute = BGPRoute.with(
                        localPref = 50,
                        asPath = pathOf(destination, BGPNode.with(2), node, sender)
                )

                protocol.onLoopDetected(node, sender, importedRoute)

                it("does not disabled the neighbor who sent the route") {
                    verify(node.routingTable, never()).disable(any())
                }
            }

            on("the LOCAL-PREF of the imported route is lower than the alternative route's and the subpath matches") {

                val importedRoute = BGPRoute.with(
                        localPref = 25,
                        asPath = pathOf(destination, BGPNode.with(2), node, sender)
                )

                protocol.onLoopDetected(node, sender, importedRoute)

                it("does not disabled the neighbor who sent the route") {
                    verify(node.routingTable, never()).disable(any())
                }
            }

            on("the LOCAL-PREF is higher than the alternative route's and the subpath does NOT match") {

                val importedRoute = BGPRoute.with(
                        localPref = 200,
                        asPath = pathOf(destination, BGPNode.with(3), node, sender)
                )

                protocol.onLoopDetected(node, sender, importedRoute)

                it("does not disabled the neighbor who sent the route") {
                    verify(node.routingTable, never()).disable(any())
                }
            }
        }
    }

})