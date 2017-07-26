package core.simulator

import bgp.BGPRoute
import bgp.ISSBGPProtocol
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import testing.*
import testing.bgp.pathOf

/**
 * Created on 26-07-2017.
 *
 * @author David Fialho
 */
object ISSBGPWithShortestPathRoutingTests : Spek({

    given("topology with non-absorbent cycle") {

        val topology = bgpTopology {
            node { 0 using ISSBGPProtocol() }
            node { 1 using ISSBGPProtocol() }
            node { 2 using ISSBGPProtocol() }
            node { 3 using ISSBGPProtocol() }

            link { 1 to 0 withCost 0 }
            link { 2 to 0 withCost 0 }
            link { 3 to 0 withCost 0 }
            link { 1 to 2 withCost 1 }
            link { 2 to 3 withCost -1 }
            link { 3 to 1 withCost 2 }
        }

        beforeEachTest {
            Scheduler.reset()
            topology.getNodes().forEach { it.reset() }
        }

        afterEachTest {
            Scheduler.reset()
        }

        val node = topology.getNodes().sortedBy { it.id }

        on("simulating with node 0 as the destination") {

            val terminated = Engine.simulate(node[0], threshold = 1000)

            it("terminates") {
                MatcherAssert.assertThat(terminated, Matchers.`is`(true))
            }

            it("finishes with node 1 selecting route with cost 0 via node 0") {
                MatcherAssert.assertThat(node[1].routingTable.getSelectedRoute(),
                        Matchers.`is`(BGPRoute.with(localPref = 0, asPath = pathOf(0))))
            }

            it("finishes with node 2 selecting route with cost 0 via node 0") {
                MatcherAssert.assertThat(node[2].routingTable.getSelectedRoute(),
                        Matchers.`is`(BGPRoute.with(localPref = 0, asPath = pathOf(0))))
            }

            it("finishes with node 3 selecting route with cost 2 via node 1") {
                MatcherAssert.assertThat(node[3].routingTable.getSelectedRoute(),
                        Matchers.`is`(BGPRoute.with(localPref = 2, asPath = pathOf(0, 1))))
            }

            it("finishes with link from 1 to 2 disabled") {
                MatcherAssert.assertThat(node[1].routingTable.table.isEnabled(node[2]), Matchers.`is`(false))
            }

            it("finishes with link from 2 to 3 disabled") {
                MatcherAssert.assertThat(node[2].routingTable.table.isEnabled(node[3]), Matchers.`is`(false))
            }

            it("finishes with link from 3 to 1 enabled") {
                MatcherAssert.assertThat(node[3].routingTable.table.isEnabled(node[1]), Matchers.`is`(true))
            }
        }
    }

})