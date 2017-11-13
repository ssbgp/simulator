package simulation

import core.routing.*
import core.simulator.Advertiser
import io.ParseException
import io.StubParser
import java.io.File
import java.io.IOException

/**
 * Created on 13-11-2017
 *
 * @author David Fialho
 *
 * The Advertiser DB is an abstraction to obtain advertising nodes from both the topology (if they
 * are included in it) and a database of stub nodes. The stub database is optional. If no database
 * is specified, advertising node will only be taken from the topology.
 *
 * @property topology   the topology to get advertisers from
 * @property stubsFile  the file specifying stubs to use as advertisers
 * @param stubsProtocol the protocol to assign to stubs
 * @param parseExtender the function used to parse extender labels to actual extenders
 */
class AdvertiserDB<R: Route>(
        val topology: Topology<R>,
        val stubsFile: File?,
        private val stubsProtocol: Protocol<R>,
        private val parseExtender: (String) -> Extender<R>
) {

    /**
     * Handler class used to create stubs found in a stubs file while it is parsed.
     */
    private class StubsCreator<R: Route>(
            private val stubIDs: List<NodeID>,
            private val topology: Topology<R>,
            private val stubsProtocol: Protocol<R>,
            private val parseExtender: (String) -> Extender<R>
    ): StubParser.Handler {

        /**
         * Maps IDs to stubs
         */
        private val stubsMap = HashMap<NodeID, Node<R>>()

        /**
         * Returns stubs found in the stubs file.
         */
        val stubs: Collection<Node<R>>
            get() = stubsMap.values

        /**
         * Invoked when a new stub item is found.
         *
         * @param id          the ID of the stub
         * @param inNeighbor  the ID of the stub's in-neighbor
         * @param label       the label of the extender associated with the link between the
         *                    neighbor and the stub
         * @param currentLine line number where the stub link was parsed
         */
        override fun onStubLink(id: NodeID, inNeighbor: NodeID, label: String, currentLine: Int) {

            // Ignore all stubs that in the stubIDs list
            if (id !in stubIDs) {
                return
            }

            val neighbor = topology[inNeighbor]

            if (neighbor == null) {
                throw ParseException("neighbor '$inNeighbor' of stub '$id' was not found in " +
                        "the topology", currentLine)
            }

            // May throw a ParseException
            // Do this before putting any stub in stubs
            val extender = parseExtender(label)

            val stub = stubsMap.getOrPut(id) { Node(id, stubsProtocol) }
            stub.addInNeighbor(neighbor, extender)
        }

    }

    /**
     * Retrieves advertisers with the IDs specified in [advertiserIDs], from the topology or the
     * stubs database. For each ID It will always check the topology first. Therefore, if the
     * topology contains a node a specified ID and the stubs database includes a stub with the same
     * ID, then the node from the topology will be used.
     *
     * @param advertiserIDs the IDs of the advertisers to retrieve
     * @throws InitializationException if it can not get all advertisers
     * @throws IOException if an IO error occurs
     * @throws ParseException if a neighbor of a stub is not included in the topology or if the
     *                        one of the extender labels is invalid
     */
    @Throws(InitializationException::class, IOException::class, ParseException::class)
    fun get(advertiserIDs: List<NodeID>): List<Advertiser<R>> {

        val advertisers = ArrayList<Node<R>>()  // holds stubs found
        val missingAdvertisers = ArrayList<NodeID>()  // stubs not found on the topology

        // Start looking for stubs in the topology
        // The topology is in memory and therefore it is usually faster to search than reading a
        // file. If all stubs are found in the topology, then we avid having to access
        // the filesystem
        for (id in advertiserIDs) {
            val advertiser = topology[id]

            if (advertiser != null) {
                advertisers.add(advertiser)
            } else {
                missingAdvertisers.add(id)
            }
        }

        // Look in stubs file only for advertisers that were not found in the topology
        if (!missingAdvertisers.isEmpty() && stubsFile != null) {
            val stubsCreator = StubsCreator(missingAdvertisers,
                    topology, stubsProtocol, parseExtender)

            StubParser(stubsFile).use {
                it.parse(stubsCreator)
            }

            advertisers.addAll(stubsCreator.stubs)
        }

        // Check if all advertisers were obtained
        if (advertiserIDs.size != advertisers.size) {
            val idsFound = advertisers.map { it.id }.toSet()
            val idsNotFound = advertiserIDs.filter { it in idsFound }

            throw InitializationException("the following advertisers " +
                    "'${idsNotFound.joinToString(limit = 5)}' were not found")
        }

        return advertisers
    }

}