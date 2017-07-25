package bgp

import core.routing.NodeID
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.jetbrains.spek.api.dsl.context
import testing.someExtender

/**
 * Created on 21-07-2017

 * @author David Fialho
 */
object BGPTopologyBuilderTests : Spek({

    /**
     * Returns a BGP link connecting two BGP nodes with the given IDs. The returned link is associated with a extender
     * obtained using the someExtender() method.
     */
    fun BGPLink(from: NodeID, to: NodeID): BGPLink {
        return BGPLink(BGPNode.with(from), BGPNode.with(to), someExtender())
    }

    given("a clean builder") {

        val builder = BGPTopologyBuilder()

        on("calling build") {

            val topology = builder.build()

            it("builds a topology of size 0") {
                assertThat(topology.size, `is`(0))
            }

            it("builds a topology with 0 links") {
                assertThat(topology.linkCount, `is`(0))
            }
        }

        on("adding a node with ID 1") {

            val added = builder.addNode(id = 1)

            it("returns true indicating the node was added") {
                assertThat(added, `is`(true))
            }

            val topology = builder.build()

            it("builds a topology of size 1") {
                assertThat(topology.size, `is`(1))
            }

            it("builds a topology containing a node with ID 1") {
                assertThat(topology.getNodes(), contains(BGPNode.with(id = 1)))
            }

            it("builds a topology with 0 links") {
                assertThat(topology.linkCount, `is`(0))
            }
        }

        on("adding second node with ID 1 again") {

            val added = builder.addNode(id = 1)

            it("returns false indicating the node was NOT added") {
                assertThat(added, `is`(false))
            }

            val topology = builder.build()

            it("builds a topology of size 1") {
                assertThat(topology.size, `is`(1))
            }
        }

        on("adding second node with ID 2") {

            val added = builder.addNode(id = 2)

            it("returns true indicating the node was added") {
                assertThat(added, `is`(true))
            }

            val topology = builder.build()

            it("builds a topology of size 2") {
                assertThat(topology.size, `is`(2))
            }

            it("builds a topology containing nodes 1 and 2") {
                assertThat(topology.getNodes(),
                    containsInAnyOrder(BGPNode.with(id = 1), BGPNode.with(id = 2)))
            }
        }

        on("adding a link from node 1 to node 2") {

            val added = builder.addLink(from = 1, to = 2, extender = someExtender())

            it("returns true indicating the link was added") {
                assertThat(added, `is`(true))
            }

            val topology = builder.build()

            it("builds a topology with 1 link") {
                assertThat(topology.linkCount, equalTo(1))
            }

            it("builds a topology with a link from 1 to 2") {
                assertThat(topology.getLinks(), contains(BGPLink(from = 1, to = 2)))
            }
        }

        on("adding a link from node 1 to node 2 again") {

            val added = builder.addLink(from = 1, to = 2, extender = someExtender())

            it("returns false indicating the link was NOT added") {
                assertThat(added, `is`(false))
            }

            val topology = builder.build()

            it("builds a topology with 1 link") {
                assertThat(topology.linkCount, equalTo(1))
            }

            it("builds a topology with a link from 1 to 2") {
                assertThat(topology.getLinks(), contains(BGPLink(from = 1, to = 2)))
            }
        }

        on("adding a link from node 2 to node 1") {

            val added = builder.addLink(from = 2, to = 1, extender = someExtender())

            it("returns true indicating the link was added") {
                assertThat(added, `is`(true))
            }

            val topology = builder.build()

            it("builds a topology with 2 links") {
                assertThat(topology.linkCount, equalTo(2))
            }

            it("builds a topology with a link from 1 to 2 and from 2 to 1") {
                assertThat(topology.getLinks(),
                    containsInAnyOrder(BGPLink(from = 1, to = 2), BGPLink(from = 2, to = 1)))
            }
        }

        on("adding a link from node 1 to node 3 (not added yet)") {

            val added = builder.addLink(from = 1, to = 3, extender = someExtender())

            it("returns false indicating the link was NOT added") {
                assertThat(added, `is`(false))
            }

            val topology = builder.build()

            it("builds a topology with size 2") {
                assertThat(topology.size, equalTo(2))
            }

            it("builds a topology with 2 links still") {
                assertThat(topology.linkCount, equalTo(2))
            }

            it("builds a topology not containing a link from 1 to 3") {
                assertThat(topology.getLinks(),
                        not(contains(BGPLink(from = 1, to = 3))))
            }
        }

        on("adding a link from node 3 (not added yet) to node 1") {

            val added = builder.addLink(from = 3, to = 1, extender = someExtender())

            it("returns false indicating the link was NOT added") {
                assertThat(added, `is`(false))
            }

            val topology = builder.build()

            it("builds a topology with size 2") {
                assertThat(topology.size, equalTo(2))
            }

            it("builds a topology with 2 links still") {
                assertThat(topology.linkCount, equalTo(2))
            }

            it("builds a topology not containing a link from 1 to 3") {
                assertThat(topology.getLinks(),
                        not(contains(BGPLink(from = 3, to = 1))))
            }
        }
    }

    context("adding new nodes with default protocol") {

        val builder = BGPTopologyBuilder()

        on("adding two nodes to the builder") {

            builder.addNode(1)
            builder.addNode(2)

            val topology = builder.build()
            val node1 = topology.getNode(1)!!
            val node2 = topology.getNode(2)!!

            it("assigns different instances of the default protocol to each node") {
                assertThat(node1.protocol !== node2.protocol, `is`(true))
            }
        }

    }

})