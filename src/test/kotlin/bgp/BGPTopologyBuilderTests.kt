package bgp

import core.routing.Extender
import core.routing.NodeID
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.jetbrains.spek.api.dsl.context

/**
 * Created on 21-07-2017

 * @author David Fialho
 */
object BGPTopologyBuilderTests : Spek({

    given("a clean builder") {

        val builder = BGPTopologyBuilder()

        on("adding a node with ID 1") {
            it("returns true indicating the node was added") {
                assertThat(builder.addNode(1), `is`(true))
            }
        }

        on("calling build") {

            val topology = builder.build()

            it("returns a topology containing a single node with ID 1") {
                assertThat(topology.getNodes(), containsInAnyOrder(BGPNodeWith(id = 1)))
            }

        }

        on("adding second node with ID 1 ") {
            val added = builder.addNode(1)

            it("returns true indicating the node was added") {
                assertThat(added, `is`(false))
            }
        }

        on("calling build") {

            val topology = builder.build()

            it("returns a topology of size 1") {
                assertThat(topology.size, equalTo(1))
            }
        }

        on("adding second node with ID 2") {
            it("returns true indicating the node was added") {
                assertThat(builder.addNode(2), `is`(true))
            }
        }

        on("calling build") {

            val topology = builder.build()

            it("returns a topology containing two nodes with IDs 1 and 2") {
                assertThat(topology.getNodes(), containsInAnyOrder(BGPNodeWith(id = 1), BGPNodeWith(id = 2)))
            }

        }

    }


})

/**
 * Returns a BGP link connecting two BGP nodes with the given IDs. The returned link is associated with a extender
 * obtained using the someExtender() method.
 */
fun BGPLink(from: NodeID, to: NodeID): BGPLink {
    return BGPLink(BGPNodeWith(from), BGPNodeWith(to), someExtender())
}

/**
 * Returns an extender when it is needed one but it is not important which one.
 */
fun someExtender(): BGPExtender {
    return FakeExtender
}

object FakeExtender : BGPExtender {
    override fun extend(route: BGPRoute): BGPRoute = invalidBGPRoute()
}
