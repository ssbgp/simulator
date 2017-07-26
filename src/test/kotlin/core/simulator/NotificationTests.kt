package core.simulator

import bgp.BGPProtocol
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

        on("simulating") {

            val topology = bgpTopology {
                node { 0 using BGPProtocol() }
                node { 1 using BGPProtocol() }

                link { 1 to 0 withCost 10 }
            }

            val node = topology.getNodes().sortedBy { it.id }

            val collector = collectBasicNotifications {
                Engine.simulate(node[0], threshold = 1000)
            }

            it("issues start notification once") {
                assertThat(collector.startNotifications.size, `is`(1))
            }

            it("issues end notification once") {
                assertThat(collector.endNotifications.size, `is`(1))
            }
        }
    }

})

