package core.simulator

import bgp.BGPProtocol
import bgp.SSBGPProtocol
import bgp.policies.interdomain.customerRoute
import bgp.policies.interdomain.peerplusRoute
import org.hamcrest.MatcherAssert.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import testing.*
import testing.bgp.pathOf
import org.hamcrest.Matchers.`is` as Is

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
object SSBGPWithInterdomainRoutingTests : Spek({

    given("loop topology with customer to destination and peer+ around the cycle") {

        val topology = bgpTopology {
            node { 0 using SSBGPProtocol() }
            node { 1 using SSBGPProtocol() }
            node { 2 using SSBGPProtocol() }
            node { 3 using SSBGPProtocol() }

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

            it("terminates") {
                assertThat(terminated,
                        Is(true))
            }

            it("finishes with node 1 selecting customer route via node 0") {
                assertThat(node[1].routingTable.getSelectedRoute(),
                        Is(customerRoute(asPath = pathOf(0))))
            }

            it("finishes with node 2 selecting customer route via node 0") {
                assertThat(node[2].routingTable.getSelectedRoute(),
                        Is(customerRoute(asPath = pathOf(0))))
            }

            it("finishes with node 3 selecting customer route via node 0") {
                assertThat(node[3].routingTable.getSelectedRoute(),
                        Is(customerRoute(asPath = pathOf(0))))
            }

            it("finishes with node 1 disabling neighbor 2") {
                assertThat(node[1].routingTable.table.isEnabled(node[2]),
                        Is(false))
            }

            it("finishes with node 2 disabling neighbor 3") {
                assertThat(node[2].routingTable.table.isEnabled(node[3]),
                        Is(false))
            }

            it("finishes with node 3 disabling neighbor 1") {
                assertThat(node[3].routingTable.table.isEnabled(node[1]),
                        Is(false))
            }
        }
    }

})