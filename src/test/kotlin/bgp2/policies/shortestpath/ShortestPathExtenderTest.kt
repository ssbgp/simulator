package bgp2.policies.shortestpath

import bgp2.BGPRoute
import core.routing2.emptyPath
import core.routing2.pathOf
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.hamcrest.Matchers.*
import org.hamcrest.MatcherAssert.assertThat
import testing2.bgp.BGPNode

/**
 * Created on 24-07-2017.

 * @author David Fialho
 */
object ShortestPathExtenderTest : Spek({

    given("a route with LOCAL-PREF = 5 and an empty AS-PATH sent by node 1234") {

        val route = BGPRoute.with(localPref = 5, asPath = emptyPath())
        val sender = BGPNode(id = 1234)

        on("extending through a link of cost 5") {

            val extendedRoute = ShortestPathExtender(cost = 5).extend(route, sender)

            it("returns a route with LOCAL-PREF = 10") {
                assertThat(extendedRoute.localPref, `is`(10))
            }

            it("returns a route with an AS-PATH = [1234]") {
                assertThat(extendedRoute.asPath, `is`(pathOf(sender)))
            }

        }

        on("extending through a link of cost 15") {

            val extendedRoute = ShortestPathExtender(cost = 15).extend(route, sender)

            it("returns a route with LOCAL-PREF = 20") {
                assertThat(extendedRoute.localPref, `is`(20))
            }

            it("returns a route with an AS-PATH = [1234]") {
                assertThat(extendedRoute.asPath, `is`(pathOf(sender)))
            }

        }

    }

    given("a route with LOCAL-PREF = 5 and an AS-PATH = [1, 2] sent by node 1234") {

        val route = BGPRoute.with(localPref = 5, asPath = pathOf(BGPNode(id = 1), BGPNode(id = 2)))
        val sender = BGPNode(id = 1234)

        on("extending through a link of cost 15") {

            val extendedRoute = ShortestPathExtender(cost = 15).extend(route, sender)

            it("returns a route with LOCAL-PREF = 20") {
                assertThat(extendedRoute.localPref, `is`(20))
            }

            it("returns a route with an AS-PATH = [1, 2, 1234]") {
                assertThat(extendedRoute.asPath, `is`(pathOf(BGPNode(1), BGPNode(2), sender)))
            }

        }

    }

    given("an invalid route sent by node 1234") {

        val route = BGPRoute.invalid()
        val sender = BGPNode(id = 1234)

        on("extending through a link of cost 15") {

            val extendedRoute = ShortestPathExtender(cost = 15).extend(route, sender)

            it("returns an invalid route") {
                assertThat(extendedRoute.isValid(), `is`(false))
            }

        }

    }

    given("a self route sent by node 1234 ") {

        val route = BGPRoute.self()
        val sender = BGPNode(id = 1234)

        on("extending through a link of cost 5") {

            val extendedRoute = ShortestPathExtender(cost = 5).extend(route, sender)

            it("returns a route with LOCAL-PREF = 5") {
                assertThat(extendedRoute.localPref, `is`(5))
            }

            it("returns a route with AS-PATH = [1234]") {
                assertThat(extendedRoute.asPath, `is`(pathOf(sender)))
            }

        }

    }

})