package bgp

import core.routing.RouteSelector
import core.routing.emptyPath
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is` as Is
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.mockito.Mockito.*
import testing.bgp.invalid
import testing.bgp.pathOf
import testing.bgp.route
import testing.mock.any

/**
 * Created on 26-07-2017
 *
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
                assertThat(learnedRoute, Is(invalid()))
            }

            it("indicates the selected route was not updated") {
                assertThat(protocol.wasSelectedRouteUpdated, Is(false))
            }
        }

        on("imported route is valid with LOCAL-PREF 10 and AS-PATH [3, 2]") {

            val importedRoute = route(localPref = 10, asPath = pathOf(3, 2))
            val learnedRoute = protocol.learn(node, sender, importedRoute)

            it("learns the imported route") {
                assertThat(learnedRoute, Is(importedRoute))
            }

            it("indicates the selected route was not updated") {
                assertThat(protocol.wasSelectedRouteUpdated, Is(false))
            }
        }

        on("imported route is valid with LOCAL-PREF 10 and AS-PATH [3, 1, 2]") {

            val importedRoute = route(localPref = 10, asPath = pathOf(3, 1, 2))
            val learnedRoute = protocol.learn(node, sender, importedRoute)

            it("learns an invalid route") {
                assertThat(learnedRoute, Is(invalid()))
            }

            it("indicates the selected route was not updated") {
                assertThat(protocol.wasSelectedRouteUpdated, Is(false))
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
                assertThat(protocol.mraiTimer.expired, Is(false))
            }

        }

        on("calling export again before the timer expires") {

            protocol.export(node)

            it("does not export the route") {
                verify(node, never()).export(route = any())
            }

            it("indicates the MRAI timer has not expired") {
                assertThat(protocol.mraiTimer.expired, Is(false))
            }
        }

        on("MRAI timer expires") {

            // emulates the expired event being taken from the scheduler
            protocol.mraiTimer.onExpired()

            it("exports the selected route to the node's neighbors") {
                verify(node, times(1)).export(selectedRoute)
            }

            it("starts the MRAI timer again") {
                assertThat(protocol.mraiTimer.expired, Is(false))
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
                assertThat(protocol.mraiTimer.expired, Is(true))
            }
        }
    }

    // TODO test process!!
})
