package bgp

import core.routing.Path
import core.routing.pathOf
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.jetbrains.spek.api.dsl.context

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

    //endregion

    context("BGP Protocol") {

        val protocol = BGPProtocol()

        context("node with ID 1 learns a route imported from node with ID 2") {

            val node = BGPNodeWith(id = 1)
            val sender = BGPNodeWith(id = 2)

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

                val importedRoute = route(localPref = 10, asPath = pathOf(BGPNodeWith(id = 3), BGPNodeWith(id = 2)))
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
                        asPath = pathOf(BGPNodeWith(id = 3), BGPNodeWith(id = 1), BGPNodeWith(id = 2))
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

    }

})

object BGPProtocolProcessTests : Spek({


})
