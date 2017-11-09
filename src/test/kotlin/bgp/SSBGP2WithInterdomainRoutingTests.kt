package bgp

import bgp.policies.interdomain.customerRoute
import bgp.policies.interdomain.peerplusRoute
import core.simulator.Engine
import org.hamcrest.MatcherAssert.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import testing.*
import testing.bgp.pathOf
import org.hamcrest.Matchers.`is` as Is

/**
 * Created on 01-09-2017
 *
 * @author David Fialho
 */
object SSBGP2WithInterdomainRoutingTests: Spek({


    given("topology with cycle with all R+ links except one R* link") {

        val topology = bgpTopology {
            node { 0 deploying SSBGP2() }
            node { 1 deploying SSBGP2() }
            node { 2 deploying SSBGP2() }
            node { 3 deploying SSBGP2() }

            customerLink { 1 to 0 }
            customerLink { 2 to 0 }
            customerLink { 3 to 0 }
            peerplusLink { 1 to 2 }
            peerplusLink { 2 to 3 }
            peerstarLink { 3 to 1 }
        }

        afterEachTest {
            Engine.scheduler.reset()
            topology.reset()
        }

        val node = topology.nodes.sortedBy { it.id }
        val protocol = node.map { it.protocol as SSBGP2 }

        on("simulating with node 0 as the destination") {

            val terminated = simulate(topology, node[0], threshold = 1000)

            it("terminates") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 1 selecting peer+ route via 2") {
                assertThat(protocol[1].routingTable.getSelectedRoute(),
                        Is(peerplusRoute(0, pathOf(0, 3, 2))))
            }

            it("finishes with node 2 selecting peer+ route via 3") {
                assertThat(protocol[2].routingTable.getSelectedRoute(),
                        Is(peerplusRoute(0, pathOf(0, 3))))
            }

            it("finishes with node 3 selecting customer route via 0") {
                assertThat(protocol[3].routingTable.getSelectedRoute(),
                        Is(customerRoute(0, pathOf(0))))
            }

            it("finishes with link from 1 to 2 enabled") {
                assertThat(protocol[1].routingTable.table.isEnabled(node[2]), Is(true))
            }

            it("finishes with link from 2 to 3 enabled") {
                assertThat(protocol[2].routingTable.table.isEnabled(node[3]), Is(true))
            }

            it("finishes with link from 3 to 1 disabled") {
                assertThat(protocol[3].routingTable.table.isEnabled(node[1]), Is(false))
            }
        }
    }

    given("topology with cycle with all R* links") {

        val topology = bgpTopology {
            node { 0 deploying SSBGP2() }
            node { 1 deploying SSBGP2() }
            node { 2 deploying SSBGP2() }
            node { 3 deploying SSBGP2() }

            customerLink { 1 to 0 }
            customerLink { 2 to 0 }
            customerLink { 3 to 0 }
            peerstarLink { 1 to 2 }
            peerstarLink { 2 to 3 }
            peerstarLink { 3 to 1 }
        }

        afterEachTest {
            Engine.scheduler.reset()
            topology.reset()
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