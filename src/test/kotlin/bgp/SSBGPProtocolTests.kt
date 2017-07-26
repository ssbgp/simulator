package bgp

import core.routing.RoutingTable
import core.routing.emptyPath
import core.routing.pathOf
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.mockito.Mockito.verify
import org.mockito.Mockito.times
import org.mockito.Mockito.never
import testing.mock.any
import testing.mock.mockNodeWith
import testing.mock.resetRoutingTable
import testing.via

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
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