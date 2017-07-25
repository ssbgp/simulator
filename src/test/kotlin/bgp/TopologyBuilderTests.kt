package bgp

import core.routing.NodeID
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import testing.someExtender

/**
 * Created on 21-07-2017

 * @author David Fialho
 */
object TopologyBuilderTests : Spek({

    /**
     * Returns a BGP link connecting two BGP nodes with the given IDs. The returned link is associated with a extender
     * obtained using the someExtender() method.
     */
    fun BGPLink(from: NodeID, to: NodeID): BGPLink {
        return BGPLink(BGPNode.with(from), BGPNode.with(to), someExtender())
    }

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
                assertThat(topology.getNodes(), containsInAnyOrder(BGPNode.with(id = 1)))
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
                assertThat(topology.getNodes(), containsInAnyOrder(BGPNode.with(id = 1), BGPNode.with(id = 2)))
            }

        }

    }

    given("a builder with two nodes with IDs 1 and 2") {

        val builder = BGPTopologyBuilder()
        builder.addNode(1)
        builder.addNode(2)

        on("adding a relationship from 1 to 2") {

            val added = builder.addLink(from = 1, to = 2, extender = someExtender())

            it("returns true") {
                assertThat(added, `is`(true))
            }

        }

        on("calling build") {
            val topology = builder.build()
            val links = topology.getLinks()

            it("returns a topology with 1 link") {
                assertThat(topology.linkCount(), equalTo(1))
            }

            it("returns a topology with a link from 1 to 2") {
                assertThat(links, contains(BGPLink(from = 1, to = 2)))
            }
        }

        on("trying to add a relationship from 1 to 2 again") {

            val added = builder.addLink(from = 1, to = 2, extender = someExtender())

            it("returns false") {
                assertThat(added, `is`(false))
            }

        }

        on("adding a relationship from 2 to 1") {

            val added = builder.addLink(from = 2, to = 1, extender = someExtender())

            it("returns true") {
                assertThat(added, `is`(true))
            }

        }

        on("calling build") {

            val topology = builder.build()
            val links = topology.getLinks()

            it("returns a topology with 2 links") {
                assertThat(topology.linkCount(), equalTo(1))
            }

            it("returns a topology with a link from 1 to 2 and another from 2 to 1") {
                assertThat(links, containsInAnyOrder(BGPLink(from = 1, to = 2), BGPLink(from = 2, to = 1)))
            }
        }

    }

})