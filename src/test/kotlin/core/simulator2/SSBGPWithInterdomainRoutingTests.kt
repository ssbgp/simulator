package core.simulator2

import bgp2.BGP
import bgp2.SSBGP
import bgp2.policies.interdomain.customerRoute
import org.hamcrest.MatcherAssert.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import testing2.*
import testing2.bgp.pathOf
import org.hamcrest.Matchers.`is` as Is

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
object SSBGPWithInterdomainRoutingTests : Spek({

    given("loop topology with customer to destination and peer+ around the cycle") {

        val topology = bgpTopology {
            node { 0 deploying SSBGP() }
            node { 1 deploying SSBGP() }
            node { 2 deploying SSBGP() }
            node { 3 deploying SSBGP() }

            customerLink { 1 to 0 }
            customerLink { 2 to 0 }
            customerLink { 3 to 0 }
            peerplusLink { 1 to 2 }
            peerplusLink { 2 to 3 }
            peerplusLink { 3 to 1 }
        }
        
        val node = topology.nodes.sortedBy { it.id }
        val protocol = node.map { it.protocol as SSBGP }

        afterEachTest {
            Engine.scheduler.reset()
            topology.nodes.forEach { it.protocol.reset() }
        }

        on("simulating with node 0 as the destination") {

            val terminated = Engine.simulate(node[0], threshold = 1000)

            it("terminates") {
                assertThat(terminated,
                        Is(true))
            }

            it("finishes with node 1 selecting customer route via node 0") {
                assertThat(protocol[1].routingTable.getSelectedRoute(),
                        Is(customerRoute(asPath = pathOf(0))))
            }

            it("finishes with node 2 selecting customer route via node 0") {
                assertThat(protocol[2].routingTable.getSelectedRoute(),
                        Is(customerRoute(asPath = pathOf(0))))
            }

            it("finishes with node 3 selecting customer route via node 0") {
                assertThat(protocol[3].routingTable.getSelectedRoute(),
                        Is(customerRoute(asPath = pathOf(0))))
            }

            it("finishes with node 1 disabling neighbor 2") {
                assertThat(protocol[1].routingTable.table.isEnabled(node[2]),
                        Is(false))
            }

            it("finishes with node 2 disabling neighbor 3") {
                assertThat(protocol[2].routingTable.table.isEnabled(node[3]),
                        Is(false))
            }

            it("finishes with node 3 disabling neighbor 1") {
                assertThat(protocol[3].routingTable.table.isEnabled(node[1]),
                        Is(false))
            }
        }
    }

    given("topology with non-absorbent cycle and with siblings") {

        val topology = bgpTopology {
            node { 0 deploying SSBGP() }
            node { 1 deploying SSBGP() }
            node { 2 deploying SSBGP() }
            node { 3 deploying SSBGP() }

            siblingLink { 1 to 0 }
            siblingLink { 2 to 0 }
            customerLink { 1 to 2 }
            customerLink { 2 to 3 }
            siblingLink { 3 to 1 }
        }

        val node = topology.nodes.sortedBy { it.id }
        val protocol = node.map { it.protocol as SSBGP }

        afterEachTest {
            Engine.scheduler.reset()
            topology.nodes.forEach { it.protocol.reset() }
        }

        on("simulating with node 0 as the destination") {

            val terminated = Engine.simulate(node[0], threshold = 1000)

            it("terminates") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 1 selecting customer route with 1 sibling hop via node 0") {
                assertThat(protocol[1].routingTable.getSelectedRoute(),
                        Is(customerRoute(siblingHops = 1, asPath = pathOf(0))))
            }

            it("finishes with node 2 selecting customer route with 1 sibling hop via node 0") {
                assertThat(protocol[2].routingTable.getSelectedRoute(),
                        Is(customerRoute(siblingHops = 1, asPath = pathOf(0))))
            }

            it("finishes with node 3 selecting customer route with 2 sibling hops via node 1") {
                assertThat(protocol[3].routingTable.getSelectedRoute(),
                        Is(customerRoute(siblingHops = 2, asPath = pathOf(0, 1))))
            }

            it("finishes with node 1 disabling neighbor 2") {
                assertThat(protocol[1].routingTable.table.isEnabled(node[2]),
                        Is(false))
            }

            it("finishes with node 2 disabling neighbor 3") {
                assertThat(protocol[2].routingTable.table.isEnabled(node[3]),
                        Is(false))
            }

            it("finishes with node 3 NOT disabling neighbor 1") {
                assertThat(protocol[3].routingTable.table.isEnabled(node[1]),
                        Is(true))
            }
        }
    }

})