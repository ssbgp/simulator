package core.simulator

import bgp.SSBGPProtocol
import bgp.policies.interdomain.customerRoute
import bgp.policies.interdomain.peerplusRoute
import org.hamcrest.MatcherAssert.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
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

    given("topology with non-absorbent cycle and with siblings") {

        val topology = bgpTopology {
            node { 0 using SSBGPProtocol() }
            node { 1 using SSBGPProtocol() }
            node { 2 using SSBGPProtocol() }
            node { 3 using SSBGPProtocol() }

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

            it("terminates") {
                assertThat(terminated, Is(true))
            }

            it("finishes with node 1 selecting customer route with 1 sibling hop via node 0") {
                assertThat(node[1].routingTable.getSelectedRoute(),
                        Is(customerRoute(siblingHops = 1, asPath = pathOf(0))))
            }

            it("finishes with node 2 selecting customer route with 1 sibling hop via node 0") {
                assertThat(node[2].routingTable.getSelectedRoute(),
                        Is(customerRoute(siblingHops = 1, asPath = pathOf(0))))
            }

            it("finishes with node 3 selecting customer route with 2 sibling hops via node 1") {
                assertThat(node[3].routingTable.getSelectedRoute(),
                        Is(customerRoute(siblingHops = 2, asPath = pathOf(0, 1))))
            }

            it("finishes with node 1 disabling neighbor 2") {
                assertThat(node[1].routingTable.table.isEnabled(node[2]),
                        Is(false))
            }

            it("finishes with node 2 disabling neighbor 3") {
                assertThat(node[2].routingTable.table.isEnabled(node[3]),
                        Is(false))
            }

            it("finishes with node 3 NOT disabling neighbor 1") {
                assertThat(node[3].routingTable.table.isEnabled(node[1]),
                        Is(true))
            }
        }
    }

    context("using re-enable timers") {

        given("topology with a single non-absorbent circuit") {

            val topology = bgpTopology {
                node { 0 using SSBGPProtocol() }
                node { 1 using SSBGPProtocol(reenableInterval = 50) }
                node { 2 using SSBGPProtocol(reenableInterval = 50) }
                node { 3 using SSBGPProtocol(reenableInterval = 50) }

                customerLink { 1 to 0 }
                peerplusLink { 1 to 2 }
                customerLink { 2 to 3 }
                customerLink { 3 to 1 }
            }

            val node = topology.getNodes().sortedBy { it.id }

            beforeEachTest {
                Scheduler.reset()
                topology.getNodes().forEach { it.reset() }
            }

            afterEachTest {
                Scheduler.reset()
            }

            on("simulating with re-enable timers") {

                var terminated = false
                val collector = collectBGPNotifications {
                    terminated = Engine.simulate(node[0], threshold = 1000)
                }

                it("terminates") {
                    assertThat(terminated, Is(true))
                }

                it("finishes with node 1 selecting customer route via node 0") {
                    assertThat(node[1].routingTable.getSelectedRoute(),
                            Is(customerRoute(asPath = pathOf(0))))
                }

                it("finishes with node 2 selecting customer route via node 3") {
                    assertThat(node[2].routingTable.getSelectedRoute(),
                            Is(customerRoute(asPath = pathOf(0, 1, 3))))
                }

                it("finishes with node 3 selecting customer route via node 1") {
                    assertThat(node[3].routingTable.getSelectedRoute(),
                            Is(customerRoute(asPath = pathOf(0, 1))))
                }

                it("finishes with node 1 NOT disabling neighbor 2") {
                    assertThat(node[1].routingTable.table.isEnabled(node[2]),
                            Is(true))
                }

                it("issued a single re-enable notification") {
                    assertThat(collector.reEnableNotifications.size,
                            Is(1))
                }
            }
        }

        given("topology with a cycle with multiple non-absorbent circuits and synced re-enable timers") {

            val topology = bgpTopology {
                node { 0 using SSBGPProtocol() }
                node { 1 using SSBGPProtocol(reenableInterval = 50) }
                node { 2 using SSBGPProtocol(reenableInterval = 50) }
                node { 3 using SSBGPProtocol(reenableInterval = 50) }

                customerLink { 1 to 0 }
                customerLink { 2 to 0 }
                peerplusLink { 1 to 2 }
                peerplusLink { 2 to 3 }
                customerLink { 3 to 1 }
            }

            val node = topology.getNodes().sortedBy { it.id }

            beforeEachTest {
                Scheduler.reset()
                node.forEach { it.reset() }
            }

            afterEachTest {
                Scheduler.reset()
            }

            on("simulating for destination 0") {

                var terminated = false
                collectBGPNotifications {
                    terminated = Engine.simulate(node[0], threshold = 1000)
                }

                it("does NOT terminate") {
                    assertThat(terminated, Is(false))
                }
            }
        }

        given("topology with a cycle with multiple non-absorbent circuits and NOT synced re-enable timers") {

            val topology = bgpTopology {
                node { 0 using SSBGPProtocol() }
                node { 1 using SSBGPProtocol(reenableInterval = 50) }
                node { 2 using SSBGPProtocol(reenableInterval = 100) }
                node { 3 using SSBGPProtocol(reenableInterval = 150) }

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
                node.forEach { it.reset() }
            }

            afterEachTest {
                Scheduler.reset()
            }

            on("simulating for destination 0 with synced re-enable timers") {

                var terminated = false
                val collector = collectBGPNotifications {
                    terminated = Engine.simulate(node[0], threshold = 1000)
                }

                it("terminates") {
                    assertThat(terminated, Is(true))
                }

                it("finishes with node 1 selecting peer+ route via node 2") {
                    assertThat(node[1].routingTable.getSelectedRoute(),
                            Is(peerplusRoute(asPath = pathOf(0, 3, 2))))
                }

                it("finishes with node 2 selecting peer+ route via node 3") {
                    assertThat(node[2].routingTable.getSelectedRoute(),
                            Is(peerplusRoute(asPath = pathOf(0, 3))))
                }

                it("finishes with node 3 selecting customer route via node 0") {
                    assertThat(node[3].routingTable.getSelectedRoute(),
                            Is(customerRoute(asPath = pathOf(0))))
                }

                it("finishes with node 1 NOT disabling neighbor 2") {
                    assertThat(node[1].routingTable.table.isEnabled(node[2]),
                            Is(true))
                }
                it("finishes with node 2 NOT disabling neighbor 3") {
                    assertThat(node[2].routingTable.table.isEnabled(node[3]),
                            Is(true))
                }
                it("finishes with node 3 NOT disabling neighbor 1") {
                    assertThat(node[3].routingTable.table.isEnabled(node[1]),
                            Is(true))
                }

                it("issued 3 re-enable notifications") {
                    assertThat(collector.reEnableNotifications.size,
                            Is(3))
                }

                it("issued 3 detect notification") {
                    assertThat(collector.detectNotifications.size,
                            Is(3))
                }
            }
        }
    }

})