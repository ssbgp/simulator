package simulation

import core.routing.*
import io.ParseException
import io.StubParser
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created on 31-08-2017
 *
 * @author David Fialho
 *
 * @property topology      the topology missing the stubs in this database
 * @property parser        the parser used to parse the stubs
 * @property parseExtender function to convert a string label into an extender
 */
class StubDB<R: Route>(
        private val topology: Topology<R>,
        private val parser: StubParser,
        private val parseExtender: (String) -> Extender<R>

): StubParser.Handler {

    /**
     * Stores the ID of the stub to get from the DB.
     */
    private var stubToGet: NodeID = -1

    /**
     * Stores the links of the stub that is to be obtained.
     */
    private val stubLinks = ArrayList<Pair<Node<R>, Extender<R>>>()

    /**
     * Obtains the stub node with id [stubID].
     *
     * @param stubID   the ID of the stub to get
     * @param protocol the protocol to assign to the stub
     * @return the stub node initialized with its in-neighbors
     * @throws ParseException if the input file defined a neighbor not included in the topology or if it includes a
     * label that is not recognized.
     */
    @Throws(ParseException::class)
    fun getStub(stubID: NodeID, protocol: Protocol<R>): Node<R>? {

        stubToGet = stubID
        parser.parse(this)

        // Return empty optional if the stub was not found
        if (stubLinks.isEmpty()) return null

        // Create stub node
        val stub = Node(stubID, protocol)
        stubLinks.forEach { (neighbor, extender) -> stub.addInNeighbor(neighbor, extender) }

        // Stub links are no longer required
        stubLinks.clear()

        // Reset the parser so that it can be parsed again
        parser.reset()

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
        if (id != stubToGet) return

        val node = topology[inNeighbor] ?:
                throw ParseException("Neighbor ID `$inNeighbor` was not found in the topology", currentLine)
        val extender = parseExtender(label)

        // Add neighbor and extender to list of stub links corresponding to the stub we want to get
        stubLinks.add(Pair(node, extender))
    }
}