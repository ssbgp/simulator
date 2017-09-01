package core.simulator

import bgp.BGP
import bgp.BGPRoute
import core.routing.pathOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is` as Is
import org.hamcrest.Matchers.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import testing.*

/**
 * Created on 26-07-2017.
 *
 * @author David Fialho
 */
object BGPWithShortestPathRoutingTests : Spek({

    given("topology with a single link from 2 to 1 with cost 10") {

        val topology = bgpTopology {
            node { 1 deploying BGP() }
            node { 2 deploying BGP() }

            link { 2 to 1 withCost 10 }
        }

        afterEachTest {
            Engine.scheduler.reset()
            topology.nodes.forEach { it.protocol.reset() }
        }

        val node1 = topology[1]!!
        val node2 = topology[2]!!
        val protocol1 = node1.protocol as BGP
        val protocol2 = node2.protocol as BGP

        on("simulating with node 1 as the destination") {

            val terminated = Engine.simulate(topology, node1, threshold = 1000)

            it("terminated") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 1 selecting self route") {
                assertThat(protocol1.routingTable.getSelectedRoute(),
                        Is(BGPRoute.self()))
            }

            it("finishes with node 1 selecting route via himself") {
                assertThat(protocol1.routingTable.getSelectedNeighbor(),
                        Is(node1))
            }

            it("finishes with node 2 selecting route with LOCAL-PREF=10 and AS-PATH=[1]") {
                assertThat(protocol2.routingTable.getSelectedRoute(),
                        Is(BGPRoute.with(10, pathOf(node1))))
            }

            it("finishes with node 2 selecting route via node 1") {
                assertThat(protocol2.routingTable.getSelectedNeighbor(),
                        Is(node1))
            }
        }

        on("simulating with node 2 as the destination") {

            val terminated = Engine.simulate(topology, node2, threshold = 1000)

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

    given("topology with 2 nodes with a link in each direction") {

        val topology = bgpTopology {
            node { 1 deploying BGP() }
            node { 2 deploying BGP() }

            link { 2 to 1 withCost 10 }
            link { 1 to 2 withCost 10 }
        }

        afterEachTest {
            Engine.scheduler.reset()
            topology.nodes.forEach { it.protocol.reset() }
        }

        val node1 = topology[1]!!
        val node2 = topology[2]!!
        val protocol1 = node1.protocol as BGP
        val protocol2 = node2.protocol as BGP

        on("simulating with node 1 as the destination") {
            // Make sure that node 1 always elects the self route

            val terminated = Engine.simulate(topology, node1, threshold = 1000)

            it("terminated") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 1 selecting self route") {
                assertThat(protocol1.routingTable.getSelectedRoute(),
                        Is(BGPRoute.self()))
            }

            it("finishes with node 1 selecting route via himself") {
                assertThat(protocol1.routingTable.getSelectedNeighbor(),
                        Is(node1))
            }

            it("finishes with node 2 selecting route with LOCAL-PREF=10 and AS-PATH=[1]") {
                assertThat(protocol2.routingTable.getSelectedRoute(),
                        Is(BGPRoute.with(10, pathOf(node1))))
            }

            it("finishes with node 2 selecting route via node 1") {
                assertThat(protocol2.routingTable.getSelectedNeighbor(),
                        Is(node1))
            }
        }
    }

    given("topology with 5 nodes forming a pyramid") {

        val topology = bgpTopology {
            node { 0 deploying BGP() }
            node { 1 deploying BGP() }
            node { 2 deploying BGP() }
            node { 3 deploying BGP() }
            node { 4 deploying BGP() }

            link { 2 to 0 withCost 0 }
            link { 2 to 3 withCost -10 }
            link { 3 to 1 withCost 0 }
            link { 3 to 2 withCost 1 }
            link { 4 to 2 withCost 10 }
            link { 4 to 3 withCost 1 }
        }

        afterEachTest {
            Engine.scheduler.reset()
            topology.nodes.forEach { it.protocol.reset() }
        }

        val node = topology.nodes.sortedBy { it.id }
        val protocol = node.map { it.protocol as BGP }

        on("simulating with node 0 as the destination") {

            val terminated = Engine.simulate(topology, node[0], threshold = 1000)

            it("terminated") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 0 selecting route via himself") {
                assertThat(protocol[0].routingTable.getSelectedRoute(),
                        Is(BGPRoute.self()))
                assertThat(protocol[0].routingTable.getSelectedNeighbor(),
                        Is(node[0]))
            }

            it("finishes with node 1 without a route") {
                assertThat(protocol[1].routingTable.getSelectedRoute(),
                        Is(BGPRoute.invalid()))
                assertThat(protocol[1].routingTable.getSelectedNeighbor(),
                        Is(nullValue()))
            }

            it("finishes with node 2 selecting route with cost 0 via node 0") {
                assertThat(protocol[2].routingTable.getSelectedRoute(),
                        Is(BGPRoute.with(0, pathOf(node[0]))))
                assertThat(protocol[2].routingTable.getSelectedNeighbor(),
                        Is(node[0]))
            }

            it("finishes with node 3 selecting route with cost 1 via node 2") {
                assertThat(protocol[3].routingTable.getSelectedRoute(),
                        Is(BGPRoute.with(1, pathOf(node[0], node[2]))))
                assertThat(protocol[3].routingTable.getSelectedNeighbor(),
                        Is(node[2]))
            }

            it("finishes with node 4 selecting route with cost 10 via node 2") {
                assertThat(protocol[4].routingTable.getSelectedRoute(),
                        Is(BGPRoute.with(10, pathOf(node[0], node[2]))))
                assertThat(protocol[4].routingTable.getSelectedNeighbor(),
                        Is(node[2]))
            }
        }

        on("simulating with node 1 as the destination") {

            val terminated = Engine.simulate(topology, node[1], threshold = 1000)

            it("terminated") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 0 without a route") {
                assertThat(protocol[0].routingTable.getSelectedRoute(),
                        Is(BGPRoute.invalid()))
                assertThat(protocol[0].routingTable.getSelectedNeighbor(),
                        Is(nullValue()))
            }

            it("finishes with node 1 selecting route via himself") {
                assertThat(protocol[1].routingTable.getSelectedRoute(),
                        Is(BGPRoute.self()))
                assertThat(protocol[1].routingTable.getSelectedNeighbor(),
                        Is(node[1]))
            }
            it("finishes with node 2 selecting route with cost -10 via node 3") {
                assertThat(protocol[2].routingTable.getSelectedRoute(),
                        Is(BGPRoute.with(-10, pathOf(node[1], node[3]))))
                assertThat(protocol[2].routingTable.getSelectedNeighbor(),
                        Is(node[3]))
            }

            it("finishes with node 3 selecting route with cost 0 via node 1") {
                assertThat(protocol[3].routingTable.getSelectedRoute(),
                        Is(BGPRoute.with(0, pathOf(node[1]))))
                assertThat(protocol[3].routingTable.getSelectedNeighbor(),
                        Is(node[1]))
            }

            it("finishes with node 4 selecting route with cost 0 via node 3") {
                assertThat(protocol[4].routingTable.getSelectedRoute(),
                        Is(BGPRoute.with(1, pathOf(node[1], node[3]))))
                assertThat(protocol[4].routingTable.getSelectedNeighbor(),
                        Is(node[3]))
            }
        }
    }

    given("topology with 4 where three form a cycle and all three have a link for node 0") {

        val topology = bgpTopology {
            node { 0 deploying BGP() }
            node { 1 deploying BGP() }
            node { 2 deploying BGP() }
            node { 3 deploying BGP() }

            link { 1 to 0 withCost 0 }
            link { 2 to 0 withCost 0 }
            link { 3 to 0 withCost 0 }
            link { 1 to 2 withCost 1 }
            link { 2 to 3 withCost -1 }
            link { 3 to 1 withCost 2 }
        }

        afterEachTest {
            Engine.scheduler.reset()
            topology.nodes.forEach { it.protocol.reset() }
        }

        val node = topology.nodes.sortedBy { it.id }

        on("simulating with node 0 as the destination") {

            val terminated = Engine.simulate(topology, node[0], threshold = 1000)

            it("does not terminate") {
                assertThat(terminated, Is(false))
            }
        }
    }

})