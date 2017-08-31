package simulation

import core.routing.*
import io.ParseException
import io.StubParser
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created on 31-08-2017
 *
 * @author David Fialho
 *
 * @param stubProtocol     the protocol to assign to the stub
 * @property parseExtender function to convert a string label into an extender
 */
class StubDB<R: Route>(
        val stubsFile: File,
        private val stubProtocol: Protocol<R>,
        private val parseExtender: (String) -> Extender<R>

): StubParser.Handler {

    private var topology: Topology<R>? = null
    private var stubID: NodeID = -1

    /**
     * Stores the links of the stub that is to be obtained.
     */
    private val stubLinks = ArrayList<Pair<Node<R>, Extender<R>>>()

    /**
     * Obtains the stub node with id [stubID].
     *
     * @param stubID   the ID of the stub to get
     * @param topology the topology missing the stubs in this database
     * @return the stub node initialized with its in-neighbors
     * @throws ParseException if the input file defined a neighbor not included in the topology or if it includes a
     * label that is not recognized.
     */
    @Throws(ParseException::class)
    fun getStub(stubID: NodeID, topology: Topology<R>): Node<R>? {

        StubParser.useFile(stubsFile).use {
            this.stubID = stubID
            this.topology = topology
            it.parse(this)
        }

        // Return empty optional if the stub was not found
        if (stubLinks.isEmpty()) return null

        // Create stub node
        val stub = Node(stubID, stubProtocol)
        stubLinks.forEach { (neighbor, extender) -> stub.addInNeighbor(neighbor, extender) }

        // Stub links are no longer required
        stubLinks.clear()

        return stub
    }

    /**
     * Invoked when a new stub item is found.
     *
     * @param id          the ID of the stub
     * @param inNeighbor  the ID of the stub's in-neighbor
     * @param label       the label of the extender associated with the link between the neighbor and the stub
     * @param currentLine line number where the stub link was parsed
     * @throws ParseException if the neighbor was not included in the topology or if it includes a
     * label that is not recognized.
     */
    @Throws(ParseException::class)
    override fun onStubLink(id: NodeID, inNeighbor: NodeID, label: String, currentLine: Int) {

        // Consider only the stub that we want to obtain
        if (id != stubID) return

        val node = topology?.get(inNeighbor) ?:
                throw ParseException("Neighbor ID `$inNeighbor` was not found in the topology", currentLine)
        val extender = parseExtender(label)

        // Add neighbor and extender to list of stub links corresponding to the stub we want to get
        stubLinks.add(Pair(node, extender))
    }
}