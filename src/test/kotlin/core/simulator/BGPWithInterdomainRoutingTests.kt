package core.simulator

import bgp.BGPProtocol
import bgp.BGPRoute
import bgp.policies.interdomain.customerRoute
import bgp.policies.interdomain.peerplusRoute
import bgp.policies.interdomain.providerRoute
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is` as Is
import testing.*
import testing.bgp.pathOf

/**
 * Created on 26-07-2017.
 *
 * @author David Fialho
 */
object BGPWithInterdomainRoutingTests : Spek({

    given("topology with a single customer link from 1 to 0") {

        val topology = bgpTopology {
            node { 0 using BGPProtocol() }
            node { 1 using BGPProtocol() }

            customerLink { 1 to 0 }
        }

        val node = topology.getNodes().sortedBy { it.id }

        beforeEachTest {
            Scheduler.reset()
            topology.getNodes().forEach { it.reset() }
        }

        afterEachTest {
            Scheduler.reset()
        }

        on("simulating with node 0 as the destination") {

            val terminated = Engine.simulate(node[0], threshold = 1000)

            it("terminated") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 0 selecting self route") {
                assertThat(node[0].routingTable.getSelectedRoute(),
                        Is(BGPRoute.self()))
            }

            it("finishes with node 1 selecting customer via node 0") {
                assertThat(node[1].routingTable.getSelectedRoute(),
                        Is(customerRoute(asPath = pathOf(0))))
            }
        }

        on("simulating with node 1 as the destination") {

            val terminated = Engine.simulate(node[1], threshold = 1000)

            it("terminated") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 0 selecting an invalid route") {
                assertThat(node[0].routingTable.getSelectedRoute(),
                        Is(BGPRoute.invalid()))
            }

            it("finishes with node 1 selecting self route") {
                assertThat(node[1].routingTable.getSelectedRoute(),
                        Is(BGPRoute.self()))
            }
        }
    }

    given("square topology") {

        val topology = bgpTopology {
            node { 0 using BGPProtocol() }
            node { 1 using BGPProtocol() }
            node { 2 using BGPProtocol() }
            node { 3 using BGPProtocol() }

            customerLink { 1 to 0 }
            peerLink { 1 to 2 }
            peerplusLink { 2 to 1 }
            providerLink { 3 to 2 }
        }

        val node = topology.getNodes().sortedBy { it.id }

        beforeEachTest {
            Scheduler.reset()
            topology.getNodes().forEach { it.reset() }
        }

        afterEachTest {
            Scheduler.reset()
        }

        on("simulating with node 0 as the destination") {

            val terminated = Engine.simulate(node[0], threshold = 1000)

            it("terminated") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 1 selecting customer route via node 0") {
                assertThat(node[1].routingTable.getSelectedRoute(),
                        Is(customerRoute(asPath = pathOf(0))))
            }

            it("finishes with node 2 selecting peer+ route via node 1") {
                assertThat(node[2].routingTable.getSelectedRoute(),
                        Is(peerplusRoute(asPath = pathOf(0, 1))))
            }

            it("finishes with node 3 selecting provider route via node 2") {
                assertThat(node[3].routingTable.getSelectedRoute(),
                        Is(providerRoute(asPath = pathOf(0, 1, 2))))
            }
        }
    }

    given("loop topology with customer to destination and peer+ around the cycle") {

        val topology = bgpTopology {
            node { 0 using BGPProtocol() }
            node { 1 using BGPProtocol() }
            node { 2 using BGPProtocol() }
            node { 3 using BGPProtocol() }

            customerLink { 1 to 0 }
            customerLink { 2 to 0 }
            customerLink { 3 to 0 }
            peerplusLink { 1 to 2 }
            peerplusLink { 2 to 3 }
            peerplusLink { 3 to 1 }
        }

        val node = topology.getNodes().sortedBy { it.id }

        beforeEachTest {
            Scheduler.reset()
            topology.getNodes().forEach { it.reset() }
        }

        afterEachTest {
            Scheduler.reset()
        }

        on("simulating with node 0 as the destination") {

            val terminated = Engine.simulate(node[0], threshold = 1000)

            it("does NOT terminate") {
                assertThat(terminated, Is(false))
            }
        }
    }

    given("topology without cycles and with siblings") {

        val topology = bgpTopology {
            node { 0 using BGPProtocol() }
            node { 1 using BGPProtocol() }
            node { 2 using BGPProtocol() }
            node { 3 using BGPProtocol() }

            siblingLink{ 1 to 0 }
            siblingLink{ 2 to 1 }
            peerplusLink { 3 to 1 }
            customerLink { 3 to 2 }
        }

        val node = topology.getNodes().sortedBy { it.id }

        beforeEachTest {
            Scheduler.reset()
            topology.getNodes().forEach { it.reset() }
        }

        afterEachTest {
            Scheduler.reset()
        }

        on("simulating with node 0 as the destination") {

            val terminated = Engine.simulate(node[0], threshold = 1000)

            it("terminates") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 1 selecting customer route with 1 sibling hop via node 0") {
                assertThat(node[1].routingTable.getSelectedRoute(),
                        Is(customerRoute(siblingHops = 1, asPath = pathOf(0))))
            }

            it("finishes with node 2 selecting customer route with 2 sibling hops via node 1") {
                assertThat(node[2].routingTable.getSelectedRoute(),
                        Is(customerRoute(siblingHops = 2, asPath = pathOf(0, 1))))
            }

            it("finishes with node 3 selecting peer+ route with 0 sibling hops via node 1") {
                assertThat(node[3].routingTable.getSelectedRoute(),
                        Is(peerplusRoute(siblingHops = 0, asPath = pathOf(0, 1))))
            }
        }
    }

    given("topology with non-absorbent cycle and with siblings") {

        val topology = bgpTopology {
            node { 0 using BGPProtocol() }
            node { 1 using BGPProtocol() }
            node { 2 using BGPProtocol() }
            node { 3 using BGPProtocol() }

            siblingLink { 1 to 0 }
            siblingLink { 2 to 0 }
            customerLink { 1 to 2 }
            customerLink { 2 to 3 }
            siblingLink { 3 to 1 }
        }

        val node = topology.getNodes().sortedBy { it.id }

        beforeEachTest {
            Scheduler.reset()
            topology.getNodes().forEach { it.reset() }
        }

        afterEachTest {
            Scheduler.reset()
        }

        on("simulating with node 0 as the destination") {

            val terminated = Engine.simulate(node[0], threshold = 1000)

            it("does NOT terminate") {
                assertThat(terminated, Is(false))
            }
        }
    }

})