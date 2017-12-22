package core.simulator

import bgp.BGP
import bgp.SSBGP
import org.hamcrest.MatcherAssert.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import testing.*
import utils.collectBGPNotifications
import org.hamcrest.Matchers.`is` as Is

/**
 * Created on 26-07-2017.
 *
 * @author David Fialho
 */
object NotificationsTests: Spek({

    context("engine") {

        on("simulating topology with only one link") {

            val topology = bgpTopology {
                node { 0 deploying BGP(mrai = 1) }
                node { 1 deploying BGP(mrai = 1) }

                link { 1 to 0 withCost 10 }
            }

            val node = topology.nodes.sortedBy { it.id }

            val collector = collectBGPNotifications {
                simulate(topology, node[0], threshold = 1000)
            }

            it("issues start notification once") {
                assertThat(collector.startNotifications.size, Is(1))
            }

            it("issues end notification once") {
                assertThat(collector.endNotifications.size, Is(1))
            }

            it("never issues threshold reached notification") {
                assertThat(collector.thresholdReachedNotifications.size, Is(0))
            }

            it("issues message sent notification once") {
                assertThat(collector.messageSentNotifications.size, Is(1))
            }

            it("issues message received notification once") {
                assertThat(collector.messageReceivedNotifications.size, Is(1))
            }

            it("issues import notification once") {
                assertThat(collector.importNotifications.size, Is(1))
            }

            it("issues learn notification twice") {
                assertThat(collector.learnNotifications.size, Is(2))
            }

            it("never issues detect notification") {
                assertThat(collector.detectNotifications.size, Is(0))
            }

            it("issues select notification twice") {
                assertThat(collector.selectNotifications.size, Is(2))
            }

            it("issues send notification 2 times") {
                // Although node 1 has no in-neighbors, two export notifications are sent
                // This is because the export notification indicates that a node exports a
                // route not that it actually sent any route to any neighbor.
                // Note that the sent notification count is only one indicating that only
                // 1 message was sent from  node 0 to node 1
                assertThat(collector.exportNotifications.size, Is(2))
            }
        }

        on("simulating topology with non-absorbent cycle with two nodes") {

            val topology = bgpTopology {
                node { 0 deploying SSBGP() }
                node { 1 deploying SSBGP() }
                node { 2 deploying SSBGP() }

                link { 1 to 0 withCost 0 }
                link { 2 to 0 withCost 0 }
                link { 1 to 2 withCost 1 }
                link { 2 to 1 withCost 1 }
            }

            val node = topology.nodes.sortedBy { it.id }

            val collector = collectBGPNotifications {
                simulate(topology, node[0], threshold = 1000)
            }

            it("never issues threshold reached notification") {
                assertThat(collector.thresholdReachedNotifications.size, Is(0))
            }

            it("issues message sent notification once") {
                assertThat(collector.messageSentNotifications.size, Is(8))
            }

            it("issues message received notification once") {
                assertThat(collector.messageReceivedNotifications.size, Is(8))
            }

            it("issues import notification 8 times") {
                assertThat(collector.importNotifications.size, Is(8))
            }

            it("issues learn notification 9 times") {
                assertThat(collector.learnNotifications.size, Is(9))
            }

            it("issues detect notification 2 times") {
                assertThat(collector.detectNotifications.size, Is(2))
            }

            it("issues select notification 7 times") {
                assertThat(collector.selectNotifications.size, Is(7))
            }

            it("issues send notification 7 times") {
                assertThat(collector.exportNotifications.size, Is(7))
            }
        }
    }
})
