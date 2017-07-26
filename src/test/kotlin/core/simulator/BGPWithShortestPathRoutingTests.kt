package core.simulator

import bgp.BGPProtocol
import bgp.BGPRoute
import core.routing.pathOf
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
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
            node { 1 using BGPProtocol() }
            node { 2 using BGPProtocol() }

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

    given("topology with 2 nodes with a link in each direction") {

        val topology = bgpTopology {
            node { 1 using BGPProtocol() }
            node { 2 using BGPProtocol() }

            link { 2 to 1 withCost 10 }
            link { 1 to 2 withCost 10 }
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
            // Make sure that node 1 always elects the self route

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
    }

    given("topology with 5 nodes forming a pyramid") {

        val topology = bgpTopology {
            node { 0 using BGPProtocol() }
            node { 1 using BGPProtocol() }
            node { 2 using BGPProtocol() }
            node { 3 using BGPProtocol() }
            node { 4 using BGPProtocol() }

            link { 2 to 0 withCost 0 }
            link { 2 to 3 withCost -10 }
            link { 3 to 1 withCost 0 }
            link { 3 to 2 withCost 1 }
            link { 4 to 2 withCost 10 }
            link { 4 to 3 withCost 1 }
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

            it("terminated") {
                MatcherAssert.assertThat(terminated, Matchers.`is`(true))
            }

            it("finishes with node 0 selecting route via himself") {
                MatcherAssert.assertThat(node[0].routingTable.getSelectedRoute(), Matchers.`is`(BGPRoute.self()))
                MatcherAssert.assertThat(node[0].routingTable.getSelectedNeighbor(), Matchers.`is`(node[0]))
            }

            it("finishes with node 1 without a route") {
                MatcherAssert.assertThat(node[1].routingTable.getSelectedRoute(), Matchers.`is`(BGPRoute.invalid()))
                MatcherAssert.assertThat(node[1].routingTable.getSelectedNeighbor(), Matchers.`is`(Matchers.nullValue()))
            }

            it("finishes with node 2 selecting route with cost 0 via node 0") {
                MatcherAssert.assertThat(node[2].routingTable.getSelectedRoute(), Matchers.`is`(BGPRoute.with(0, pathOf(node[0]))))
                MatcherAssert.assertThat(node[2].routingTable.getSelectedNeighbor(), Matchers.`is`(node[0]))
            }

            it("finishes with node 3 selecting route with cost 1 via node 2") {
                MatcherAssert.assertThat(node[3].routingTable.getSelectedRoute(),
                        Matchers.`is`(BGPRoute.with(1, pathOf(node[0], node[2]))))
                MatcherAssert.assertThat(node[3].routingTable.getSelectedNeighbor(), Matchers.`is`(node[2]))
            }

            it("finishes with node 4 selecting route with cost 10 via node 2") {
                MatcherAssert.assertThat(node[4].routingTable.getSelectedRoute(),
                        Matchers.`is`(BGPRoute.with(10, pathOf(node[0], node[2]))))
                MatcherAssert.assertThat(node[4].routingTable.getSelectedNeighbor(), Matchers.`is`(node[2]))
            }
        }

        on("simulating with node 1 as the destination") {

            val terminated = Engine.simulate(node[1], threshold = 1000)

            it("terminated") {
                MatcherAssert.assertThat(terminated, Matchers.`is`(true))
            }

            it("finishes with node 0 without a route") {
                MatcherAssert.assertThat(node[0].routingTable.getSelectedRoute(), Matchers.`is`(BGPRoute.invalid()))
                MatcherAssert.assertThat(node[0].routingTable.getSelectedNeighbor(), Matchers.`is`(Matchers.nullValue()))
            }

            it("finishes with node 1 selecting route via himself") {
                MatcherAssert.assertThat(node[1].routingTable.getSelectedRoute(), Matchers.`is`(BGPRoute.self()))
                MatcherAssert.assertThat(node[1].routingTable.getSelectedNeighbor(), Matchers.`is`(node[1]))
            }
            it("finishes with node 2 selecting route with cost -10 via node 3") {
                MatcherAssert.assertThat(node[2].routingTable.getSelectedRoute(),
                        Matchers.`is`(BGPRoute.with(-10, pathOf(node[1], node[3]))))
                MatcherAssert.assertThat(node[2].routingTable.getSelectedNeighbor(), Matchers.`is`(node[3]))
            }

            it("finishes with node 3 selecting route with cost 0 via node 1") {
                MatcherAssert.assertThat(node[3].routingTable.getSelectedRoute(), Matchers.`is`(BGPRoute.with(0, pathOf(node[1]))))
                MatcherAssert.assertThat(node[3].routingTable.getSelectedNeighbor(), Matchers.`is`(node[1]))
            }

            it("finishes with node 4 selecting route with cost 0 via node 3") {
                MatcherAssert.assertThat(node[4].routingTable.getSelectedRoute(),
                        Matchers.`is`(BGPRoute.with(1, pathOf(node[1], node[3]))))
                MatcherAssert.assertThat(node[4].routingTable.getSelectedNeighbor(), Matchers.`is`(node[3]))
            }
        }
    }

    given("topology with 4 where three form a cycle and all three have a link for node 0") {

        val topology = bgpTopology {
            node { 0 using BGPProtocol() }
            node { 1 using BGPProtocol() }
            node { 2 using BGPProtocol() }
            node { 3 using BGPProtocol() }

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

            it("does not terminate") {
                MatcherAssert.assertThat(terminated, Matchers.`is`(false))
            }
        }
    }

})