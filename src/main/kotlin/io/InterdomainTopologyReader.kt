package io

import bgp.*
import core.routing.*
import core.simulator.Time
import utils.toNonNegativeInt
import java.io.*

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 */
class InterdomainTopologyReader(reader: Reader, private val forcedMRAI: Time? = null)
    : TopologyReader<BGPRoute>, Closeable {

    /**
     * Provides option to create a reader with a file object.
     */
    @Throws(FileNotFoundException::class)
    constructor(file: File, forcedMRAI: Time? = null): this(FileReader(file), forcedMRAI)

    private val parser = TopologyParser(reader)

    private inner class InterdomainHandler(val builder: TopologyBuilder<BGPRoute>)
        : TopologyParser.Handler {

        /**
         * Invoked when reading the stream when a new node item is read.
         *
         * @param id          the ID of the node parse
         * @param values      sequence of values associated with the node
         * @param currentLine line number where the node was parsed
         */
        override fun onNodeItem(id: NodeID, values: List<String>, currentLine: Int) {

            if (values.size < 2) {
                throw ParseException("node is missing required values: Protocol and/or MRAI", currentLine)
            }

            val protocolLabel = values[0]

            // Use the "forced MRAI" value if set. Otherwise, use the value specified in the topology file.
            val mrai = forcedMRAI ?: try {
                values[1].toNonNegativeInt()
            } catch (e: NumberFormatException) {
                throw ParseException("MRAI must be a non-negative integer number, but was ${values[1]}", currentLine)
            }

            // TODO @refactor - make this case sensitive
            val protocol = when (protocolLabel.toLowerCase()) {
                "bgp" -> BGP(mrai)
                "ssbgp" -> SSBGP(mrai)
                "issbgp" -> ISSBGP(mrai)
                "ssbgp2" -> SSBGP2(mrai)
                "issbgp2" -> ISSBGP2(mrai)
                else -> throw ParseException(
                        "protocol label `$protocolLabel` was not recognized: supported labels are BGP, " +
                                "SSBGP, ISSBGP, SSBGP2, and ISSBGP2", currentLine)
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
                throw ParseException("link is missing extender label value", currentLine)
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
    }

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
        val builder = TopologyBuilder<BGPRoute>()
        parser.parse(InterdomainHandler(builder))
        return builder.build()
    }

    /**
     * Closes the stream and releases any system resources associated with it.
     */
    override fun close() {
        parser.close()
    }
}