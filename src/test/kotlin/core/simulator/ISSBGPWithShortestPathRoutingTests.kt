package core.simulator

import bgp.BGPRoute
import bgp.ISSBGP
import org.hamcrest.MatcherAssert.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import testing.*
import testing.bgp.pathOf
import org.hamcrest.Matchers.`is` as Is

/**
 * Created on 26-07-2017.
 *
 * @author David Fialho
 */
object ISSBGPWithShortestPathRoutingTests : Spek({

    given("topology with non-absorbent cycle") {

        val topology = bgpTopology {
            node { 0 deploying ISSBGP() }
            node { 1 deploying ISSBGP() }
            node { 2 deploying ISSBGP() }
            node { 3 deploying ISSBGP() }

            link { 1 to 0 withCost 0 }
            link { 2 to 0 withCost 0 }
            link { 3 to 0 withCost 0 }
            link { 1 to 2 withCost 1 }
            link { 2 to 3 withCost -1 }
            link { 3 to 1 withCost 2 }
        }

        afterEachTest {
            Simulator.scheduler.reset()
            topology.nodes.forEach { it.protocol.reset() }
        }

        val node = topology.nodes.sortedBy { it.id }
        val protocol = node.map { it.protocol as ISSBGP }

        on("simulating with node 0 as the destination") {

            val terminated = simulate(topology, node[0], threshold = 1000)

            it("terminates") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 1 selecting route with cost 0 via node 0") {
                assertThat(protocol[1].routingTable.getSelectedRoute(),
                        Is(BGPRoute.with(localPref = 0, asPath = pathOf(0))))
            }

            it("finishes with node 2 selecting route with cost 0 via node 0") {
                assertThat(protocol[2].routingTable.getSelectedRoute(),
                        Is(BGPRoute.with(localPref = 0, asPath = pathOf(0))))
            }

            it("finishes with node 3 selecting route with cost 2 via node 1") {
                assertThat(protocol[3].routingTable.getSelectedRoute(),
                        Is(BGPRoute.with(localPref = 2, asPath = pathOf(0, 1))))
            }

            it("finishes with link from 1 to 2 disabled") {
                assertThat(protocol[1].routingTable.table.isEnabled(node[2]), Is(false))
            }

            it("finishes with link from 2 to 3 disabled") {
                assertThat(protocol[2].routingTable.table.isEnabled(node[3]), Is(false))
            }

            it("finishes with link from 3 to 1 enabled") {
                assertThat(protocol[3].routingTable.table.isEnabled(node[1]), Is(true))
            }
        }
    }

})