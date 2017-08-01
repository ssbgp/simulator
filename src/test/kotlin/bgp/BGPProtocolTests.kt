package bgp

import com.nhaarman.mockito_kotlin.*
import core.routing.RouteSelector
import core.routing.emptyPath
import core.simulator.Engine
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is` as Is
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import testing.`when`
import testing.bgp.invalid
import testing.bgp.pathOf
import testing.bgp.route
import testing.its
import testing.then

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
object BGPProtocolTests : Spek({

    /**
     * Sets a fake selected route to a fake node. The node calling this method should be a mock or a spy.
     */
    fun BGPNode.setSelectedRoute(route: BGPRoute) {

        // Create a fake selector that returns always the same selected route
        val selector: RouteSelector<BGPNode, BGPRoute> = mock {
            on { getSelectedRoute() } doReturn route
        }

        // Inject the route selector into the node
        whenever(this.routingTable).thenReturn(selector)
    }

    given("a node with ID 1 learns a route from node with ID 2") {

        val protocol = BGPProtocol()
        val node = BGPNode.with(id = 1)
        val sender = BGPNode.with(id = 2)

        `when`("the imported route is invalid") {

            val learnedRoute = protocol.learn(node, sender, invalid())

            it("learns an invalid route") {
                assertThat(learnedRoute, Is(invalid()))
            }
        }

        `when`("the imported route has LOCAL-PREF 10 and AS-PATH [3, 2]") {

            val importedRoute = route(localPref = 10, asPath = pathOf(3, 2))
            val learnedRoute = protocol.learn(node, sender, importedRoute)

            it("learns the imported route") {
                assertThat(learnedRoute, Is(importedRoute))
            }
        }

        `when`("the imported route has LOCAL-PREF 10 and AS-PATH [3, 1, 2]") {

            val importedRoute = route(localPref = 10, asPath = pathOf(3, 1, 2))
            val learnedRoute = protocol.learn(node, sender, importedRoute)

            it("learns an invalid route") {
                assertThat(learnedRoute, Is(invalid()))
            }
        }
    }

    context("Export method without MRAI") {

    }

    context("Export method with MRAI") {

        given("a node that has never exported a route") {

            val node: BGPNode = mock()
            val protocol = BGPProtocol(mrai = 10)
            val selectedRoute = route(localPref = 100, asPath = emptyPath())

            beforeEachTest {
                // Reset the number of calls to export
                reset(node)
                node.setSelectedRoute(selectedRoute)
            }

            afterEachTest { Engine.scheduler.reset() }

            then("its MRAI timer is currently expired") {
                assertThat(protocol.mraiTimer.expired, Is(true))
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

                its("MRAI timer has not expired") {
                    assertThat(protocol.mraiTimer.expired, Is(false))
                }
            }

            `when`("its MRAI timer expires") {

                // emulates the expired event being taken from the scheduler
                protocol.mraiTimer.onExpired()

                it("does NOT export any route because the selected route did not change") {
                    verify(node, never()).export(any())
                }

                it("keeps the MRAI timer expired") {
                    assertThat(protocol.mraiTimer.expired, Is(true))
                }
            }
        }
    }

})
