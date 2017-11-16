package io

import bgp.*
import bgp.policies.interdomain.*
import core.routing.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.instanceOf
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.junit.jupiter.api.Assertions.assertThrows
import testing.`when`
import testing.bgp.BGPNode
import testing.then
import java.io.StringReader
import org.hamcrest.Matchers.`is` as Is

object InterdomainTopologyReaderTest: Spek({

    given("an interdomain topology file") {

        data class ExpectedNode(val id: NodeID, val protocol: Protocol<BGPRoute>, val mrai: Int)
        val correctNodeLines = listOf(
                Pair("node = 10 | BGP | 5000", ExpectedNode(id = 10, protocol = BGP(), mrai = 5000)),
                Pair("node = 10 | BGP | 0", ExpectedNode(id = 10, protocol = BGP(), mrai = 0)),
                // Extra values are ignored
                Pair("node = 10 | BGP | 5000 | 35", ExpectedNode(id = 10, protocol = BGP(), mrai = 5000)),
                Pair("node = 10 | SSBGP | 5000", ExpectedNode(id = 10, protocol = SSBGP(), mrai = 5000)),
                Pair("node = 10 | ISSBGP | 5000", ExpectedNode(id = 10, protocol = ISSBGP(), mrai = 5000))
        )

        correctNodeLines.forEach { (line, expected) ->

            `when`("topology is read from file with a single node `$line`") {

                var nullableTopology: Topology<BGPRoute>? = null

                InterdomainTopologyReader(StringReader(line)).use {
                    nullableTopology = it.read()
                }

                val topology = nullableTopology!!

                it("has size 1") {
                    assertThat(topology.size, Is(1))
                }

                it("has a node with ID ${expected.id}") {
                    assertThat(BGPNode(expected.id) in topology.nodes, Is(true))
                }

                val node = topology[expected.id]!!

                it("has node that deploys ${expected.protocol}") {
                    assertThat(node.protocol,
                            Is(instanceOf(expected.protocol::class.java)))
                }

                it("has node with MRAI ${expected.mrai}") {
                    assertThat((node.protocol as BaseBGP).mrai,
                            Is(expected.mrai))
                }
            }
        }

        data class ExpectedLink(val tail: NodeID, val head: NodeID, val extender: Extender<BGPRoute>)
        val correctLinkLines = listOf(
                Pair("link = 10 | 11 | C", ExpectedLink(tail = 10, head = 11, extender = CustomerExtender)),
                // Extra values are ignored
                Pair("link = 10 | 11 | C | 35", ExpectedLink(tail = 10, head = 11, extender = CustomerExtender)),
                Pair("link = 10 | 11 | R+", ExpectedLink(tail = 10, head = 11, extender = PeerplusExtender)),
                Pair("link = 10 | 11 | R", ExpectedLink(tail = 10, head = 11, extender = PeerExtender)),
                Pair("link = 10 | 11 | P", ExpectedLink(tail = 10, head = 11, extender = ProviderExtender)),
                Pair("link = 10 | 11 | S", ExpectedLink(tail = 10, head = 11, extender = SiblingExtender))
        )

        fun lines(vararg lines: String): String = lines.joinToString("\n")

        correctLinkLines.forEach { (line, expected) ->

            `when`("topology is read from file with `node = ${expected.tail}`, `node = ${expected.head}`, `$line`") {

                val content = lines(
                        "node = ${expected.tail} | BGP | 10",
                        "node = ${expected.head} | BGP | 10",
                        line
                )
                var nullableTopology: Topology<BGPRoute>? = null

                InterdomainTopologyReader(StringReader(content)).use {
                    nullableTopology = it.read()
                }

                val topology = nullableTopology!!

                it("has size 2") {
                    assertThat(topology.size, Is(2))
                }

                it("has a node with ID ${expected.tail}") {
                    assertThat(BGPNode(expected.tail) in topology.nodes, Is(true))
                }

                it("has a node with ID ${expected.head}") {
                    assertThat(BGPNode(expected.head) in topology.nodes, Is(true))
                }

                val tail = topology[expected.tail]!!
                val head = topology[expected.head]!!

                it("has link from node ${expected.tail} to node ${expected.head} of type ${expected.extender}") {
                    assertThat(topology.links, contains(Link(tail, head, expected.extender)))
                }
            }
        }

        val incorrectLines = listOf(
                "node = 10 | proto | 5000",
                "node = 10 | BGP | -1",
                "node = 10 | BGP | ",
                "node = 10 | BGP",
                "link = 10 | 11 |",
                "link = 10 | 11 | abc"
        )

        incorrectLines.forEach { line ->

            `when`("topology is read from file with incorrect line `$line`") {

                var exception: ParseException? = null

                it("throws a ParseException") {
                    InterdomainTopologyReader(StringReader(line)).use {
                        exception = assertThrows(ParseException::class.java) {
                            it.read()
                        }
                    }
                }

                it("indicates the error is in line 1") {
                    assertThat(exception?.lineNumber, Is(1))
                }
            }
        }

        `when`("a fixed MRAI value is set") {

            val content = lines(
                    "node = 1 | BGP | 5000",
                    "node = 2 | BGP | 1234"
            )
            val topology = InterdomainTopologyReader(StringReader(content), forcedMRAI = 10).use {
                it.read()
            }

            then("all nodes are initialized with that MRAI value") {
                topology.nodes.asSequence()
                        .map { it.protocol as BGP }
                        .forEach { assertThat(it.mrai, Is(10)) }
            }
        }
    }

})