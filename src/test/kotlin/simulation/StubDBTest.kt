package simulation

import bgp.BGP
import bgp.BGPRoute
import bgp.policies.interdomain.CustomerExtender
import bgp.policies.interdomain.PeerExtender
import bgp.policies.interdomain.ProviderExtender
import core.routing.Extender
import core.routing.NodeID
import core.routing.TopologyBuilder
import io.StubParser
import io.parseInterdomainExtender
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.nullValue
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.StringReader
import org.hamcrest.Matchers.`is` as Is

/**
 * Created on 31-08-2017
 *
 * @author David Fialho
 */
object StubDBTest: Spek({

    fun lines(vararg lines: String): String = lines.joinToString("\n")

    given("file with single lines `1 | 2 | C`, `1 | 3 | R`, `1 | 4 | P`") {

        val fileContent = lines(
                "1 | 2 | C",
                "1 | 3 | R",
                "1 | 4 | P"
        )

        val topology = TopologyBuilder<BGPRoute>()
                .addNode(2, BGP())
                .addNode(3, BGP())
                .addNode(4, BGP())
                .build()

        on("getting stub with ID 1") {

            val stub = StubParser(StringReader(fileContent)).use {
                StubDB(topology, it, ::parseInterdomainExtender).getStub(stubID = 1, protocol = BGP())!!
            }

            it("obtained a stub with three in-neighbors") {
                assertThat(stub.inNeighbors.size, Is(3))
            }

            val inNeighborsIDs = stub.inNeighbors.map { it.node.id }.sorted()
            val inNeighborsExtenders: Map<NodeID, Extender<BGPRoute>> = stub.inNeighbors.map { it.node.id to it.extender }.toMap()

            it("obtained a stub with in-neighbor 2") {
                assertThat(2 in inNeighborsIDs, Is(true))
            }

            it("obtained a stub with customer extender from neighbor 2") {
                assertThat(inNeighborsExtenders[2], Is(CustomerExtender as Extender<BGPRoute>))
            }

            it("obtained a stub with in-neighbor 3") {
                assertThat(3 in inNeighborsIDs, Is(true))
            }

            it("obtained a stub with peer extender from neighbor 3") {
                assertThat(inNeighborsExtenders[3], Is(PeerExtender as Extender<BGPRoute>))
            }

            it("obtained a stub with in-neighbor 4") {
                assertThat(4 in inNeighborsIDs, Is(true))
            }

            it("obtained a stub with provider extender from neighbor 4") {
                assertThat(inNeighborsExtenders[4], Is(ProviderExtender as Extender<BGPRoute>))
            }

        }

        on("getting stub with ID 3") {

            val stub = StubParser(StringReader(fileContent)).use {
                StubDB(topology, it, ::parseInterdomainExtender).getStub(stubID = 3, protocol = BGP())
            }

            it("did not obtain a stub") {
                assertThat(stub, Is(nullValue()))
            }

        }
    }
})