package io

import bgp.BGP
import bgp.BGPRoute
import bgp.ISSBGP
import bgp.SSBGP
import bgp.policies.interdomain.*
import core.routing.*
import io.TopologyParser.Handler
import java.io.*

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 */
class InterdomainTopologyReader(reader: Reader): TopologyReader, Closeable, Handler {

    /**
     * Provides option to create a reader with a file object.
     */
    @Throws(FileNotFoundException::class)
    constructor(file: File): this(FileReader(file))

    private val builder = TopologyBuilder<BGPRoute>()
    private val parser = TopologyParser(reader, this)

    /**
     * Returns a Topology object that is represented in the input source.
     *
     * The topology object uses a BGP like protocol and the extenders assigned to the links are defined in the
     * interdomain routing policies.
     *
     * @throws IOException    If an I/O error occurs
     * @throws ParseException if a topology object can not be created due to incorrect representation
     */
    @Throws(IOException::class, ParseException::class)
    override fun read(): Topology<BGPRoute> {
        parser.parse()
        return builder.build()
    }

    /**
     * Invoked when reading the stream when a new node item is read.
     *
     * @param id          the ID of the node parse
     * @param values      sequence of values associated with the node
     * @param currentLine line number where the node was parsed
     */
    override fun onNodeItem(id: NodeID, values: List<String>, currentLine: Int) {

        if (values.size < 2) {
            throw ParseException("Node is missing required values: Protocol and/or MRAI", currentLine)
        }

        val protocolLabel = values[0]
        val mrai = parseNonNegativeInteger(values[1], currentLine)

        val protocol = when (protocolLabel.toLowerCase()) {
            "bgp" -> BGP(mrai)
            "ssbgp" -> SSBGP(mrai)
            "issbgp" -> ISSBGP(mrai)
            else -> throw ParseException("Protocol label `$protocolLabel` was not recognized: supported labels are BGP, " +
                    "SSBGP, and ISSBGP", currentLine)
        }

        try {
            builder.addNode(id, protocol)

        } catch (e: ElementExistsException) {
            throw ParseException(e.message!!, currentLine)
        }
    }

    /**
     * Invoked when reading the stream when a new link item is read.
     *
     * @param tail        the ID of the tail node
     * @param head        the ID of the head node
     * @param values      sequence of values associated with the link item
     * @param currentLine line number where the node was parsed
     */
    override fun onLinkItem(tail: NodeID, head: NodeID, values: List<String>, currentLine: Int) {

        if (values.isEmpty()) {
            throw ParseException("Link is missing required values: extender label", currentLine)
        }

        val extender = parseExtender(values[0], currentLine)

        try {
            builder.link(tail, head, extender)

        } catch (e: ElementNotFoundException) {
            throw ParseException(e.message!!, currentLine)
        } catch (e: ElementExistsException) {
            throw ParseException(e.message!!, currentLine)
        }
    }

    @Throws(ParseException::class)
    private fun parseNonNegativeInteger(value: String, currentLine: Int): Int {

        try {
            val intValue = value.toInt()
            if (intValue < 0) {
                throw NumberFormatException()
            }

            return intValue

        } catch (e: NumberFormatException) {
            throw ParseException("Failed to parse value `$value`: must be a non-negative integer value", currentLine)
        }
    }

    @Throws(ParseException::class)
    private fun parseExtender(label: String, currentLine: Int): Extender<BGPRoute> {

        return when (label) {
            "r+" -> PeerplusExtender
            "c" -> CustomerExtender
            "r" -> PeerExtender
            "p" -> ProviderExtender
            "s" -> SiblingExtender
            else -> throw ParseException("Extender label `$label` was not recognized: " +
                    "must be either R+, C, R, P, or S", currentLine)
        }
    }

    /**
     * Closes the stream and releases any system resources associated with it.
     */
    override fun close() {
        parser.close()
    }
}