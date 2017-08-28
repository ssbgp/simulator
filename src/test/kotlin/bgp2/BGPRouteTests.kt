package bgp2

import core.routing2.emptyPath
import core.routing2.pathOf
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import testing2.bgp.BGPNode

/**
 * Created on 21-07-2017

 * @author David Fialho
 */
object BGPRouteTests : Spek({

    given("route1 has higher LOCAL-PREF than route2") {

        it("returns positive value if length of the AS-PATH of route1 is shorter than route2's") {
            val route1 = BGPRoute.with(localPref = 10, asPath = pathOf(BGPNode(id = 1)))
            val route2 = BGPRoute.with(localPref = 5, asPath = pathOf(BGPNode(id = 1), BGPNode(id = 2)))

            assertThat(bgpRouteCompare(route1, route2), greaterThan(0))
        }

        it("returns positive value if length of the AS-PATH of route1 is equal than route2's") {
            val route1 = BGPRoute.with(localPref = 10, asPath = pathOf(BGPNode(id = 1)))
            val route2 = BGPRoute.with(localPref = 5, asPath = pathOf(BGPNode(id = 1)))

            assertThat(bgpRouteCompare(route1, route2), greaterThan(0))
        }

        it("returns positive value if length of the AS-PATH of route1 is longer than route2's") {
            val route1 = BGPRoute.with(localPref = 10, asPath = pathOf(BGPNode(id = 1)))
            val route2 = BGPRoute.with(localPref = 5, asPath = pathOf(BGPNode(id = 1), BGPNode(id = 2)))

            assertThat(bgpRouteCompare(route1, route2), greaterThan(0))
        }

    }

    given("route1 has same LOCAL-PREF as route2 and they have different AS-PATH lengths") {

        it("returns positive value if length of the AS-PATH of route1 is shorter than route2's") {
            val route1 = BGPRoute.with(localPref = 10, asPath = pathOf(BGPNode(id = 1)))
            val route2 = BGPRoute.with(localPref = 10, asPath = pathOf(BGPNode(id = 1), BGPNode(id = 2)))

            assertThat(bgpRouteCompare(route1, route2), greaterThan(0))
        }

        it("returns negative value if length of the AS-PATH of route1 is longer than route2's") {
            val route1 = BGPRoute.with(localPref = 10, asPath = pathOf(BGPNode(id = 1), BGPNode(id = 2)))
            val route2 = BGPRoute.with(localPref = 10, asPath = pathOf(BGPNode(id = 1)))

            assertThat(bgpRouteCompare(route1, route2), lessThan(0))
        }

    }

    given("routes 1 and 2 have the same LOCAL-PREF the same AS-PATH length") {

        it("returns positive value if the ID of the next-hop node of route1 is lower than route2's") {
            val route1 = BGPRoute.with(localPref = 10, asPath = pathOf(BGPNode(id = 1), BGPNode(id = 2)))
            val route2 = BGPRoute.with(localPref = 10, asPath = pathOf(BGPNode(id = 2), BGPNode(id = 3)))

            assertThat(bgpRouteCompare(route1, route2), greaterThan(0))
        }

        it("returns negative value if the ID of the next-hop node of route1 is higher than route2's") {
            val route1 = BGPRoute.with(localPref = 10, asPath = pathOf(BGPNode(id = 2), BGPNode(id = 3)))
            val route2 = BGPRoute.with(localPref = 10, asPath = pathOf(BGPNode(id = 1), BGPNode(id = 2)))

            assertThat(bgpRouteCompare(route1, route2), lessThan(0))
        }

        it("returns zero if the ID of the next-hop node of route1 is equal to route2's") {
            val route1 = BGPRoute.with(localPref = 10, asPath = pathOf(BGPNode(id = 2), BGPNode(id = 1)))
            val route2 = BGPRoute.with(localPref = 10, asPath = pathOf(BGPNode(id = 3), BGPNode(id = 1)))

            assertThat(bgpRouteCompare(route1, route2), equalTo(0))
        }

        it("returns zero if both AS-PATH are empty") {
            val route1 = BGPRoute.with(localPref = 10, asPath = emptyPath())
            val route2 = BGPRoute.with(localPref = 10, asPath = emptyPath())

            assertThat(bgpRouteCompare(route1, route2), equalTo(0))
        }

    }

})
