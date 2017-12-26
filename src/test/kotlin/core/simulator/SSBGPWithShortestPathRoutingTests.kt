package core.simulator

import bgp.BGPRoute
import bgp.SSBGP
import core.routing.pathOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
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
object SSBGPWithShortestPathRoutingTests : Spek({

    given("topology with a single link from 2 to 1 with cost 10") {

        val topology = bgpTopology {
            node { 1 deploying SSBGP() }
            node { 2 deploying SSBGP() }

            link { 2 to 1 withCost 10 }
        }

        afterEachTest {
            Simulator.scheduler.reset()
            topology.nodes.forEach { it.protocol.reset() }
        }

        val node1 = topology[1]!!
        val node2 = topology[2]!!
        val protocol1 = node1.protocol as SSBGP
        val protocol2 = node2.protocol as SSBGP

        on("simulating with node 1 as the destination") {

            val terminated = simulate(topology, node1, threshold = 1000)

            it("terminated") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 1 selecting self route") {
                assertThat(protocol1.routingTable.getSelectedRoute(), Is(BGPRoute.self()))
            }

            it("finishes with node 1 selecting route via himself") {
                assertThat(protocol1.routingTable.getSelectedNeighbor(), Is(node1))
            }

            it("finishes with node 2 selecting route with LOCAL-PREF=10 and AS-PATH=[1]") {
                assertThat(protocol2.routingTable.getSelectedRoute(), Is(BGPRoute.with(10, pathOf(node1))))
            }

            it("finishes with node 2 selecting route via node 1") {
                assertThat(protocol2.routingTable.getSelectedNeighbor(), Is(node1))
            }
        }

        on("simulating with node 2 as the destination") {

            val terminated = simulate(topology, node2, threshold = 1000)

            it("terminated") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 1 selecting an invalid route") {
                assertThat(protocol1.routingTable.getSelectedRoute(), Is(BGPRoute.invalid()))
            }

            it("finishes with node 1 selecting null neighbor") {
                assertThat(protocol1.routingTable.getSelectedNeighbor(), Is(nullValue()))
            }

            it("finishes with node 2 selecting self route") {
                assertThat(protocol2.routingTable.getSelectedRoute(), Is(BGPRoute.self()))
            }

            it("finishes with node 2 selecting route via himself") {
                assertThat(protocol2.routingTable.getSelectedNeighbor(), Is(node2))
            }
        }
    }

    given("topology with 4 where three form a cycle and all three have a link for node 0") {

        val topology = bgpTopology {
            node { 0 deploying SSBGP() }
            node { 1 deploying SSBGP() }
            node { 2 deploying SSBGP() }
            node { 3 deploying SSBGP() }

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
        val protocol = node.map { it.protocol as SSBGP }

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

    given("topology with absorbent cycle") {

        val topology = bgpTopology {
            node { 0 deploying SSBGP() }
            node { 1 deploying SSBGP() }
            node { 2 deploying SSBGP() }
            node { 3 deploying SSBGP() }

            link { 1 to 0 withCost 0 }
            link { 2 to 0 withCost 0 }
            link { 3 to 0 withCost 0 }
            link { 1 to 2 withCost -3 }
            link { 2 to 3 withCost 1 }
            link { 3 to 1 withCost 2 }
        }

        afterEachTest {
            Simulator.scheduler.reset()
            topology.nodes.forEach { it.protocol.reset() }
        }

        val node = topology.nodes.sortedBy { it.id }
        val protocol = node.map { it.protocol as SSBGP }

        on("simulating with node 0 as the destination") {

            val terminated = simulate(topology, node[0], threshold = 1000)

            it("terminates") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 1 selecting route with cost 0 via node 0") {
                assertThat(protocol[1].routingTable.getSelectedRoute(),
                        Is(BGPRoute.with(localPref = 0, asPath = pathOf(0))))
            }

            it("finishes with node 2 selecting route with cost 3 via node 3") {
                assertThat(protocol[2].routingTable.getSelectedRoute(),
                        Is(BGPRoute.with(localPref = 3, asPath = pathOf(0, 1, 3))))
            }

            it("finishes with node 3 selecting route with cost 2 via node 1") {
                assertThat(protocol[3].routingTable.getSelectedRoute(),
                        Is(BGPRoute.with(localPref = 2, asPath = pathOf(0, 1))))
            }

            it("finishes with link from 1 to 2 enabled") {
                assertThat(protocol[1].routingTable.table.isEnabled(node[2]), Is(true))
            }

            it("finishes with link from 2 to 3 enabled") {
                assertThat(protocol[2].routingTable.table.isEnabled(node[3]), Is(true))
            }

            it("finishes with link from 3 to 1 enabled") {
                assertThat(protocol[3].routingTable.table.isEnabled(node[1]), Is(true))
            }
        }
    }

})