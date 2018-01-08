package core.simulator

import bgp.BGP
import bgp.BGPRoute
import bgp.policies.interdomain.customerRoute
import bgp.policies.interdomain.peerplusRoute
import bgp.policies.interdomain.providerRoute
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
object BGPWithInterdomainRoutingTests : Spek({

    given("topology with a single customer link from 1 to 0") {

        val topology = bgpTopology {
            node { 0 deploying BGP() }
            node { 1 deploying BGP() }

            customerLink { 1 to 0 }
        }

        afterEachTest {
            Simulator.scheduler.reset()
            topology.nodes.forEach { it.reset() }
        }

        val node = topology.nodes.sortedBy { it.id }
        val protocol = node.map { it.protocol as BGP }

        on("simulating with node 0 as the destination") {

            val terminated = simulate(topology, node[0], threshold = 1000)

            it("terminated") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 0 selecting self route") {
                assertThat(protocol[0].routingTable.getSelectedRoute(),
                        Is(BGPRoute.self()))
            }

            it("finishes with node 1 selecting customer via node 0") {
                assertThat(protocol[1].routingTable.getSelectedRoute(),
                        Is(customerRoute(asPath = pathOf(0))))
            }
        }

        on("simulating with node 1 as the destination") {

            val terminated = simulate(topology, node[1], threshold = 1000)

            it("terminated") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 0 selecting an invalid route") {
                assertThat(protocol[0].routingTable.getSelectedRoute(),
                        Is(BGPRoute.invalid()))
            }

            it("finishes with node 1 selecting self route") {
                assertThat(protocol[1].routingTable.getSelectedRoute(),
                        Is(BGPRoute.self()))
            }
        }
    }

    given("multiple relationships topology") {

        val topology = bgpTopology {
            node { 0 deploying BGP() }
            node { 1 deploying BGP() }
            node { 2 deploying BGP() }
            node { 3 deploying BGP() }

            customerLink { 1 to 0 }
            peerLink { 1 to 2 }
            peerplusLink { 2 to 1 }
            providerLink { 3 to 2 }
        }

        afterEachTest {
            Simulator.scheduler.reset()
            topology.nodes.forEach { it.reset() }
        }

        val node = topology.nodes.sortedBy { it.id }
        val protocol = node.map { it.protocol as BGP }

        on("simulating with node 0 as the destination") {

            val terminated = simulate(topology, node[0], threshold = 1000)

            it("terminated") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 1 selecting customer route via node 0") {
                assertThat(protocol[1].routingTable.getSelectedRoute(),
                        Is(customerRoute(asPath = pathOf(0))))
            }

            it("finishes with node 2 selecting peer+ route via node 1") {
                assertThat(protocol[2].routingTable.getSelectedRoute(),
                        Is(peerplusRoute(asPath = pathOf(0, 1))))
            }

            it("finishes with node 3 selecting provider route via node 2") {
                assertThat(protocol[3].routingTable.getSelectedRoute(),
                        Is(providerRoute(asPath = pathOf(0, 1, 2))))
            }
        }
    }

    given("square topology") {

        val topology = bgpTopology {
            node { 0 deploying BGP() }
            node { 1 deploying BGP() }
            node { 2 deploying BGP() }
            node { 3 deploying BGP() }

            customerLink { 3 to 1 }
            customerLink { 2 to 0 }
            customerLink { 2 to 3 }
            customerLink { 3 to 2 }
            peerplusLink { 0 to 3 }
        }

        afterEachTest {
            Simulator.scheduler.reset()
            topology.nodes.forEach { it.reset() }
        }

        val node = topology.nodes.sortedBy { it.id }
        val protocol = node.map { it.protocol as BGP }

        on("simulating with nodes 0 and 1 advertising peer+ routes at 0 and 100") {

            val advertisements = listOf(
                    Advertisement(node[0], peerplusRoute(), time = 0),
                    Advertisement(node[1], peerplusRoute(), time = 100)
            )

            val terminated = Simulator.simulate(topology, advertisements, threshold = 1000)

            it("terminated") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 0 selecting peer+ route via himself") {
                assertThat(protocol[0].routingTable.getSelectedRoute(),
                        Is(peerplusRoute()))
                assertThat(protocol[0].routingTable.getSelectedNeighbor(),
                        Is(node[0]))
            }

            it("finishes with node 1 selecting peer+ route via himself") {
                assertThat(protocol[1].routingTable.getSelectedRoute(),
                        Is(peerplusRoute()))
                assertThat(protocol[1].routingTable.getSelectedNeighbor(),
                        Is(node[1]))
            }

            it("finishes with node 2 selecting customer route via node 0") {
                assertThat(protocol[2].routingTable.getSelectedRoute(),
                        Is(customerRoute(asPath = pathOf(0))))
                assertThat(protocol[2].routingTable.getSelectedNeighbor(),
                        Is(node[0]))
            }

            it("finishes with node 3 selecting customer route via node 1") {
                assertThat(protocol[3].routingTable.getSelectedRoute(),
                        Is(customerRoute(asPath = pathOf(1))))
                assertThat(protocol[3].routingTable.getSelectedNeighbor(),
                        Is(node[1]))
            }
        }

        on("simulating with nodes 0 and 1 advertising customer routes at 0 and 100") {

            val advertisements = listOf(
                    Advertisement(node[0], customerRoute(), time = 0),
                    Advertisement(node[1], customerRoute(), time = 100)
            )

            val terminated = Simulator.simulate(topology, advertisements, threshold = 1000)

            it("terminated") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 0 selecting peer+ route via node 3") {
                assertThat(protocol[0].routingTable.getSelectedRoute(),
                        Is(peerplusRoute(asPath = pathOf(1, 3))))
                assertThat(protocol[0].routingTable.getSelectedNeighbor(),
                        Is(node[3]))
            }

            it("finishes with node 1 selecting customer route via himself") {
                assertThat(protocol[1].routingTable.getSelectedRoute(),
                        Is(customerRoute()))
                assertThat(protocol[1].routingTable.getSelectedNeighbor(),
                        Is(node[1]))
            }

            it("finishes with node 2 selecting customer route via node 3") {
                assertThat(protocol[2].routingTable.getSelectedRoute(),
                        Is(customerRoute(asPath = pathOf(1, 3))))
                assertThat(protocol[2].routingTable.getSelectedNeighbor(),
                        Is(node[3]))
            }

            it("finishes with node 3 selecting customer route via node 1") {
                assertThat(protocol[3].routingTable.getSelectedRoute(),
                        Is(customerRoute(asPath = pathOf(1))))
                assertThat(protocol[3].routingTable.getSelectedNeighbor(),
                        Is(node[1]))
            }
        }
    }

    given("loop topology with customer to destination and peer+ around the cycle") {

        val topology = bgpTopology {
            node { 0 deploying BGP() }
            node { 1 deploying BGP() }
            node { 2 deploying BGP() }
            node { 3 deploying BGP() }

            customerLink { 1 to 0 }
            customerLink { 2 to 0 }
            customerLink { 3 to 0 }
            peerplusLink { 1 to 2 }
            peerplusLink { 2 to 3 }
            peerplusLink { 3 to 1 }
        }

        afterEachTest {
            Simulator.scheduler.reset()
            topology.nodes.forEach { it.reset() }
        }

        val node = topology.nodes.sortedBy { it.id }
        val protocol = node.map { it.protocol as BGP }

        on("simulating with node 0 as the destination") {

            val terminated = simulate(topology, node[0], threshold = 1000)

            it("does NOT terminate") {
                assertThat(terminated, Is(false))
            }
        }


        on("simulating with nodes 1, 2, and 3 advertising customer routes at time 0") {

            val advertisements = listOf(
                    Advertisement(node[1], customerRoute()),
                    Advertisement(node[2], customerRoute()),
                    Advertisement(node[3], customerRoute())
            )

            val terminated = Simulator.simulate(topology, advertisements, threshold = 1000)

            it("does not terminate") {
                assertThat(terminated, Is(false))
            }
        }

        on("simulating with nodes 1, 2, and 3 advertising customer routes at times 0, 100, and 200") {

            val advertisements = listOf(
                    Advertisement(node[1], customerRoute(), time = 0),
                    Advertisement(node[2], customerRoute(), time = 100),
                    Advertisement(node[3], customerRoute(), time = 200)
            )

            val terminated = Simulator.simulate(topology, advertisements, threshold = 1000)

            it("does not terminate") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 1 selecting a customer route via himself") {
                assertThat(protocol[1].routingTable.getSelectedRoute(),
                        Is(customerRoute()))
                assertThat(protocol[1].routingTable.getSelectedNeighbor(),
                        Is(node[1]))
            }

            it("finishes with node 2 selecting peer+ route via node 3") {
                assertThat(protocol[2].routingTable.getSelectedRoute(),
                        Is(peerplusRoute(asPath = pathOf(1, 3))))
                assertThat(protocol[2].routingTable.getSelectedNeighbor(),
                        Is(node[3]))
            }

            it("finishes with node 3 selecting peer+ route via node 1") {
                assertThat(protocol[3].routingTable.getSelectedRoute(),
                        Is(peerplusRoute(asPath = pathOf(1))))
                assertThat(protocol[3].routingTable.getSelectedNeighbor(),
                        Is(node[1]))
            }
        }
    }

    given("topology without cycles and with siblings") {

        val topology = bgpTopology {
            node { 0 deploying BGP() }
            node { 1 deploying BGP() }
            node { 2 deploying BGP() }
            node { 3 deploying BGP() }

            siblingLink { 1 to 0 }
            siblingLink { 2 to 1 }
            peerplusLink { 3 to 1 }
            customerLink { 3 to 2 }
        }

        afterEachTest {
            Simulator.scheduler.reset()
            topology.nodes.forEach { it.reset() }
        }

        val node = topology.nodes.sortedBy { it.id }
        val protocol = node.map { it.protocol as BGP }

        on("simulating with node 0 as the destination") {

            val terminated = simulate(topology, node[0], threshold = 1000)

            it("terminates") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 1 selecting customer route with 1 sibling hop via node 0") {
                assertThat(protocol[1].routingTable.getSelectedRoute(),
                        Is(customerRoute(siblingHops = 1, asPath = pathOf(0))))
            }

            it("finishes with node 2 selecting customer route with 2 sibling hops via node 1") {
                assertThat(protocol[2].routingTable.getSelectedRoute(),
                        Is(customerRoute(siblingHops = 2, asPath = pathOf(0, 1))))
            }

            it("finishes with node 3 selecting peer+ route with 0 sibling hops via node 1") {
                assertThat(protocol[3].routingTable.getSelectedRoute(),
                        Is(peerplusRoute(siblingHops = 0, asPath = pathOf(0, 1))))
            }
        }
    }

    given("topology with non-absorbent cycle and with siblings") {

        val topology = bgpTopology {
            node { 0 deploying BGP() }
            node { 1 deploying BGP() }
            node { 2 deploying BGP() }
            node { 3 deploying BGP() }

            siblingLink { 1 to 0 }
            siblingLink { 2 to 0 }
            customerLink { 1 to 2 }
            customerLink { 2 to 3 }
            siblingLink { 3 to 1 }
        }

        afterEachTest {
            Simulator.scheduler.reset()
            topology.nodes.forEach { it.reset() }
        }

        val node = topology.nodes.sortedBy { it.id }

        on("simulating with node 0 as the destination") {

            val terminated = simulate(topology, node[0], threshold = 1000)

            it("does NOT terminate") {
                assertThat(terminated, Is(false))
            }
        }
    }

})