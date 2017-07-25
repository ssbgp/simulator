package core.simulator

import bgp.BGPProtocol
import bgp.BGPRoute
import core.routing.pathOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import testing.*

/**
 * Created on 24-07-2017.
 *
 * @author David Fialho
 */
object EngineTests : Spek({

    context("shortest-path routing") {

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
                    assertThat(terminated, `is`(true))
                }

                it("finishes with node 1 selecting self route") {
                    assertThat(node1.routingTable.getSelectedRoute(), `is`(BGPRoute.self()))
                }

                it("finishes with node 1 selecting route via himself") {
                    assertThat(node1.routingTable.getSelectedNeighbor(), `is`(node1))
                }

                it("finishes with node 2 selecting route with LOCAL-PREF=10 and AS-PATH=[1]") {
                    assertThat(node2.routingTable.getSelectedRoute(), `is`(BGPRoute.with(10, pathOf(node1))))
                }

                it("finishes with node 2 selecting route via node 1") {
                    assertThat(node2.routingTable.getSelectedNeighbor(), `is`(node1))
                }
            }

            on("simulating with node 2 as the destination") {

                val terminated = Engine.simulate(node2, threshold = 1000)

                it("terminated") {
                    assertThat(terminated, `is`(true))
                }

                it("finishes with node 1 selecting an invalid route") {
                    assertThat(node1.routingTable.getSelectedRoute(), `is`(BGPRoute.invalid()))
                }

                it("finishes with node 1 selecting null neighbor") {
                    assertThat(node1.routingTable.getSelectedNeighbor(), `is`(nullValue()))
                }

                it("finishes with node 2 selecting self route") {
                    assertThat(node2.routingTable.getSelectedRoute(), `is`(BGPRoute.self()))
                }

                it("finishes with node 2 selecting route via himself") {
                    assertThat(node2.routingTable.getSelectedNeighbor(), `is`(node2))
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
                    assertThat(terminated, `is`(true))
                }

                it("finishes with node 1 selecting self route") {
                    assertThat(node1.routingTable.getSelectedRoute(), `is`(BGPRoute.self()))
                }

                it("finishes with node 1 selecting route via himself") {
                    assertThat(node1.routingTable.getSelectedNeighbor(), `is`(node1))
                }

                it("finishes with node 2 selecting route with LOCAL-PREF=10 and AS-PATH=[1]") {
                    assertThat(node2.routingTable.getSelectedRoute(), `is`(BGPRoute.with(10, pathOf(node1))))
                }

                it("finishes with node 2 selecting route via node 1") {
                    assertThat(node2.routingTable.getSelectedNeighbor(), `is`(node1))
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
                    assertThat(terminated, `is`(true))
                }

                it("finishes with node 0 selecting route via himself") {
                    assertThat(node[0].routingTable.getSelectedRoute(), `is`(BGPRoute.self()))
                    assertThat(node[0].routingTable.getSelectedNeighbor(), `is`(node[0]))
                }

                it("finishes with node 1 without a route") {
                    assertThat(node[1].routingTable.getSelectedRoute(), `is`(BGPRoute.invalid()))
                    assertThat(node[1].routingTable.getSelectedNeighbor(), `is`(nullValue()))
                }

                it("finishes with node 2 selecting route with cost 0 via node 0") {
                    assertThat(node[2].routingTable.getSelectedRoute(), `is`(BGPRoute.with(0, pathOf(node[0]))))
                    assertThat(node[2].routingTable.getSelectedNeighbor(), `is`(node[0]))
                }

                it("finishes with node 3 selecting route with cost 1 via node 2") {
                    assertThat(node[3].routingTable.getSelectedRoute(),
                            `is`(BGPRoute.with(1, pathOf(node[0], node[2]))))
                    assertThat(node[3].routingTable.getSelectedNeighbor(), `is`(node[2]))
                }

                it("finishes with node 4 selecting route with cost 10 via node 2") {
                    assertThat(node[4].routingTable.getSelectedRoute(),
                            `is`(BGPRoute.with(10, pathOf(node[0], node[2]))))
                    assertThat(node[4].routingTable.getSelectedNeighbor(), `is`(node[2]))
                }
            }

            on("simulating with node 1 as the destination") {

                val terminated = Engine.simulate(node[1], threshold = 1000)

                it("terminated") {
                    assertThat(terminated, `is`(true))
                }

                it("finishes with node 0 without a route") {
                    assertThat(node[0].routingTable.getSelectedRoute(), `is`(BGPRoute.invalid()))
                    assertThat(node[0].routingTable.getSelectedNeighbor(), `is`(nullValue()))
                }

                it("finishes with node 1 selecting route via himself") {
                    assertThat(node[1].routingTable.getSelectedRoute(), `is`(BGPRoute.self()))
                    assertThat(node[1].routingTable.getSelectedNeighbor(), `is`(node[1]))
                }
                it("finishes with node 2 selecting route with cost -10 via node 3") {
                    assertThat(node[2].routingTable.getSelectedRoute(),
                            `is`(BGPRoute.with(-10, pathOf(node[1], node[3]))))
                    assertThat(node[2].routingTable.getSelectedNeighbor(), `is`(node[3]))
                }

                it("finishes with node 3 selecting route with cost 0 via node 1") {
                    assertThat(node[3].routingTable.getSelectedRoute(), `is`(BGPRoute.with(0, pathOf(node[1]))))
                    assertThat(node[3].routingTable.getSelectedNeighbor(), `is`(node[1]))
                }

                it("finishes with node 4 selecting route with cost 0 via node 3") {
                    assertThat(node[4].routingTable.getSelectedRoute(),
                            `is`(BGPRoute.with(1, pathOf(node[1], node[3]))))
                    assertThat(node[4].routingTable.getSelectedNeighbor(), `is`(node[3]))
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
                    assertThat(terminated, `is`(false))
                }
            }
        }
    }

})