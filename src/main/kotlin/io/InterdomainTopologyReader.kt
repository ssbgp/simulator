package io

import bgp.BGP
import bgp.BGPRoute
import bgp.ISSBGP
import bgp.SSBGP
import core.routing.*
import io.TopologyParser.Handler
import utils.toNonNegativeInt
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
        val mrai = try {
            values[1].toNonNegativeInt()
        } catch (e: NumberFormatException) {
            throw ParseException("Failed to parse `${values[1]}`: must be a non-negative integer number", currentLine)
        }

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

        val extender = parseInterdomainExtender(values[0], currentLine)

        try {
            builder.link(tail, head, extender)

        } catch (e: ElementNotFoundException) {
            throw ParseException(e.message!!, currentLine)
        } catch (e: ElementExistsException) {
            throw ParseException(e.message!!, currentLine)
        }
    }

    /**
     * Closes the stream and releases any system resources associated with it.
     */
    override fun close() {
        parser.close()
    }
}