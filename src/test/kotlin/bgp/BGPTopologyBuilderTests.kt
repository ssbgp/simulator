package bgp

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*

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

        on("adding a node with ID 1 and calling build") {

            builder.addNode(1)
            val topology = builder.build()

            it("returns a topology containing a single node with ID 1") {
                assertThat(topology.getNodes(), containsInAnyOrder(BGPNodeWith(id = 1)))
            }

        }

        on("adding two nodes with IDs 1 and 2 and calling build") {

            builder.addNode(1)
            builder.addNode(2)
            val topology = builder.build()

            it("returns a topology containing two nodes with IDs 1 and 2") {
                assertThat(topology.getNodes(), containsInAnyOrder(BGPNodeWith(id = 1), BGPNodeWith(id = 2)))
            }

        }

    }

    given("a builder already containing one node with ID 1") {

        val builder = BGPTopologyBuilder()
        builder.addNode(1)

        on("adding a second node with ID 1") {
            it("returns false") {
                assertThat(builder.addNode(1), `is`(false))
            }
        }

        on("adding a second node with ID 2") {
            it("returns true") {
                assertThat(builder.addNode(2), `is`(true))
            }
        }
    }

})