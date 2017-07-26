package core.simulator

import bgp.BGPRoute
import bgp.SSBGPProtocol
import core.routing.pathOf
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import testing.bgp.pathOf
import testing.*

/**
 * Created on 26-07-2017.
 *
 * @author David Fialho
 */
object SSBGPWithShortestPathRoutingTests : Spek({

    given("topology with a single link from 2 to 1 with cost 10") {

        val topology = bgpTopology {
            node { 1 using SSBGPProtocol() }
            node { 2 using SSBGPProtocol() }

            link { 2 to 1 withCost 10 }
        }

        beforeEachTest {
            Scheduler.reset()
            topology.getNodes().forEach { it.reset() }
        }

        afterEachTest {
            Scheduler.reset()
        }

        val node1 = topology[1]!!
        val node2 = topology[2]!!

        on("simulating with node 1 as the destination") {

            val terminated = Engine.simulate(node1, threshold = 1000)

            it("terminated") {
                MatcherAssert.assertThat(terminated, Matchers.`is`(true))
            }

            it("finishes with node 1 selecting self route") {
                MatcherAssert.assertThat(node1.routingTable.getSelectedRoute(), Matchers.`is`(BGPRoute.self()))
            }

            it("finishes with node 1 selecting route via himself") {
                MatcherAssert.assertThat(node1.routingTable.getSelectedNeighbor(), Matchers.`is`(node1))
            }

            it("finishes with node 2 selecting route with LOCAL-PREF=10 and AS-PATH=[1]") {
                MatcherAssert.assertThat(node2.routingTable.getSelectedRoute(), Matchers.`is`(BGPRoute.with(10, pathOf(node1))))
            }

            it("finishes with node 2 selecting route via node 1") {
                MatcherAssert.assertThat(node2.routingTable.getSelectedNeighbor(), Matchers.`is`(node1))
            }
        }

        on("simulating with node 2 as the destination") {

            val terminated = Engine.simulate(node2, threshold = 1000)

            it("terminated") {
                MatcherAssert.assertThat(terminated, Matchers.`is`(true))
            }

            it("finishes with node 1 selecting an invalid route") {
                MatcherAssert.assertThat(node1.routingTable.getSelectedRoute(), Matchers.`is`(BGPRoute.invalid()))
            }

            it("finishes with node 1 selecting null neighbor") {
                MatcherAssert.assertThat(node1.routingTable.getSelectedNeighbor(), Matchers.`is`(Matchers.nullValue()))
            }

            it("finishes with node 2 selecting self route") {
                MatcherAssert.assertThat(node2.routingTable.getSelectedRoute(), Matchers.`is`(BGPRoute.self()))
            }

            it("finishes with node 2 selecting route via himself") {
                MatcherAssert.assertThat(node2.routingTable.getSelectedNeighbor(), Matchers.`is`(node2))
            }
        }
    }

    given("topology with 4 where three form a cycle and all three have a link for node 0") {

        val topology = bgpTopology {
            node { 0 using SSBGPProtocol() }
            node { 1 using SSBGPProtocol() }
            node { 2 using SSBGPProtocol() }
            node { 3 using SSBGPProtocol() }

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

    given("topology with absorbent cycle") {

        val topology = bgpTopology {
            node { 0 using SSBGPProtocol() }
            node { 1 using SSBGPProtocol() }
            node { 2 using SSBGPProtocol() }
            node { 3 using SSBGPProtocol() }

            link { 1 to 0 withCost 0 }
            link { 2 to 0 withCost 0 }
            link { 3 to 0 withCost 0 }
            link { 1 to 2 withCost -3 }
            link { 2 to 3 withCost 1 }
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

            it("finishes with node 2 selecting route with cost 3 via node 3") {
                MatcherAssert.assertThat(node[2].routingTable.getSelectedRoute(),
                        Matchers.`is`(BGPRoute.with(localPref = 3, asPath = pathOf(0, 1, 3))))
            }

            it("finishes with node 3 selecting route with cost 2 via node 1") {
                MatcherAssert.assertThat(node[3].routingTable.getSelectedRoute(),
                        Matchers.`is`(BGPRoute.with(localPref = 2, asPath = pathOf(0, 1))))
            }

            it("finishes with link from 1 to 2 enabled") {
                MatcherAssert.assertThat(node[1].routingTable.table.isEnabled(node[2]), Matchers.`is`(true))
            }

            it("finishes with link from 2 to 3 enabled") {
                MatcherAssert.assertThat(node[2].routingTable.table.isEnabled(node[3]), Matchers.`is`(true))
            }

            it("finishes with link from 3 to 1 enabled") {
                MatcherAssert.assertThat(node[3].routingTable.table.isEnabled(node[1]), Matchers.`is`(true))
            }
        }
    }

})