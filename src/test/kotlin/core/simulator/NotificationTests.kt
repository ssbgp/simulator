package core.simulator

import bgp.BGPProtocol
import bgp.SSBGPProtocol
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import testing.*

/**
 * Created on 26-07-2017.
 *
 * @author David Fialho
 */
object NotificationsTests : Spek({

    context("engine") {

        on("simulating topology with only one link") {

            val topology = bgpTopology {
                node { 0 using BGPProtocol() }
                node { 1 using BGPProtocol() }

                link { 1 to 0 withCost 10 }
            }

            val node = topology.getNodes().sortedBy { it.id }

            val collector = collectBGPNotifications {
                Engine.simulate(node[0], threshold = 1000)
            }

            it("issues start notification once") {
                assertThat(collector.startNotifications.size, `is`(1))
            }

            it("issues end notification once") {
                assertThat(collector.endNotifications.size, `is`(1))
            }

            it("issues a message received notification once") {
                assertThat(collector.messageReceivedNotifications.size, `is`(1))
            }

            it("issues import notification once") {
                assertThat(collector.importNotifications.size, `is`(1))
            }

            it("issues learn notification once") {
                assertThat(collector.learnNotifications.size, `is`(1))
            }

            it("never issues detect notification") {
                assertThat(collector.detectNotifications.size, `is`(0))
            }

            it("issues select notification once") {
                assertThat(collector.selectNotifications.size, `is`(1))
            }

            it("issues export notification once") {
                assertThat(collector.exportNotifications.size, `is`(1))
            }

            it("issues a message sent notification once") {
                assertThat(collector.messageSentNotifications.size, `is`(1))
            }
        }

        on("simulating topology with non-absorbent cycle with two nodes") {

            val topology = bgpTopology {
                node { 0 using SSBGPProtocol() }
                node { 1 using SSBGPProtocol() }
                node { 2 using SSBGPProtocol() }

                link { 1 to 0 withCost 0 }
                link { 2 to 0 withCost 0 }
                link { 1 to 2 withCost 1 }
                link { 2 to 1 withCost 1 }
            }

            val node = topology.getNodes().sortedBy { it.id }

            val collector = collectBGPNotifications {
                Engine.simulate(node[0], threshold = 1000)
            }

            it("issues a message received notification 8 times") {
                assertThat(collector.messageReceivedNotifications.size, `is`(8))
            }

            it("issues import notification 8 times") {
                assertThat(collector.importNotifications.size, `is`(8))
            }

            it("issues learn notification 8 times") {
                assertThat(collector.learnNotifications.size, `is`(8))
            }

            it("issues detect notification 2 times") {
                assertThat(collector.detectNotifications.size, `is`(2))
            }

            it("issues select notification 6 times") {
                assertThat(collector.selectNotifications.size, `is`(6))
            }

            it("issues export notification 7 times") {
                assertThat(collector.exportNotifications.size, `is`(7))
            }

            it("issues a message sent notification 8 times") {
                assertThat(collector.messageSentNotifications.size, `is`(8))
            }
        }
    }

})

