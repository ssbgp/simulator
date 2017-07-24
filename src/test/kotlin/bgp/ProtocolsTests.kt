package bgp

import core.routing.Path
import core.routing.RouteSelector
import core.routing.emptyPath
import core.routing.pathOf
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.on
import org.mockito.Mockito
import org.mockito.Mockito.*

/**
 * Created on 21-07-2017

 * @author David Fialho
 */
object BGPProtocolTests : Spek({

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

    //endregion

    context("BGP Protocol") {

        context("node with ID 1 learns a route imported from node with ID 2") {

            val protocol = BGPProtocol()
            val node = BGPNode.with(id = 1)
            val sender = BGPNode.with(id = 2)

            given("imported route is invalid") {

                val learnedRoute = protocol.learn(node, sender, invalid())

                it("learns an invalid route") {
                    assertThat(learnedRoute, `is`(invalid()))
                }

                it("indicates the selected route was not updated") {
                    assertThat(protocol.wasSelectedRouteUpdated, `is`(false))
                }
            }

            given("imported route is valid with LOCAL-PREF 10 and AS-PATH [3, 2]") {

                val importedRoute = route(localPref = 10, asPath = pathOf(BGPNode.with(id = 3), BGPNode.with(id = 2)))
                val learnedRoute = protocol.learn(node, sender, importedRoute)

                it("learns the imported route") {
                    assertThat(learnedRoute, `is`(importedRoute))
                }

                it("indicates the selected route was not updated") {
                    assertThat(protocol.wasSelectedRouteUpdated, `is`(false))
                }
            }

            given("imported route is valid with LOCAL-PREF 10 and AS-PATH [3, 1, 2]") {

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
    }

})