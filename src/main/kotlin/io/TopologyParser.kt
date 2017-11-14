package io

import core.routing.NodeID
import utils.toNonNegativeInt
import java.io.Closeable
import java.io.IOException
import java.io.Reader

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 */
class TopologyParser(reader: Reader): Closeable {

    /**
     * Handlers are notified once a new topology item (a node or a link) is parsed.
     * Subclasses should implement how these items should be handled.
     */
    interface Handler {

        /**
         * Invoked when reading the stream when a new node item is read.
         *
         * @param id          the ID of the node parse
         * @param values      sequence of values associated with the node
         * @param currentLine line number where the node was parsed
         */
        fun onNodeItem(id: NodeID, values: List<String>, currentLine: Int)

        /**
         * Invoked when reading the stream when a new link item is read.
         *
         * @param tail        the ID of the tail node
         * @param head        the ID of the head node
         * @param values      sequence of values associated with the link
         * @param currentLine line number where the node was parsed
         */
        fun onLinkItem(tail: NodeID, head: NodeID, values: List<String>, currentLine: Int)

    }

    private class KeyValueHandler(val handler: TopologyParser.Handler): KeyValueParser.Handler {

        /**
         * Invoked when a new entry is parsed.
         *
         * @param entry       the parsed entry
         * @param currentLine line number where the node was parsed
         */
        override fun onEntry(entry: KeyValueParser.Entry, currentLine: Int) {

            val values = entry.values

            when (entry.key.toLowerCase()) {
                "node" -> {

                    // The first value is the node ID - this value is mandatory
                    if (values.isEmpty() || (values.size == 1 && values[0].isEmpty())) {
                        throw ParseException("node entry is missing the ID value", currentLine)
                    }

                    val nodeID = parseNodeID(values[0], currentLine)

                    // The remaining values should be parsed by the Topology Reader according to
                    // its required specifications
                    handler.onNodeItem(nodeID, values.subList(1, values.lastIndex + 1), currentLine)
                }
                "link" -> {

                    // The first two values are the tail and head nodes of the link
                    if (values.size < 2 || values[0].isBlank() || values[1].isBlank()) {
                        throw ParseException("link entry is missing required values: tail node ID and/or head node ID",
                                currentLine)
                    }

                    val tailID = parseNodeID(values[0], currentLine)
                    val headID = parseNodeID(values[1], currentLine)

                    handler.onLinkItem(tailID, headID, values.subList(2, values.lastIndex + 1), currentLine)
                }
                else -> {
                    throw ParseException("invalid key `${entry.key}`: supported keys are 'node' or 'link'", currentLine)
                }

            }
        }

        private fun parseNodeID(value: String, currentLine: Int): Int {

            try {
                return value.toNonNegativeInt()
            } catch (e: NumberFormatException) {
                throw ParseException("a node ID must be a non-negative value, but was `$value`", currentLine)
            }
        }
    }

    /**
     * The topology parser is based on a key-value parser.
     * It uses this parser to handle identifying entries.
     */
    private val parser = KeyValueParser(reader)

    /**
     * Parses the stream invoking the handler once a new node or link is parsed.
     *
     * @throws IOException    If an I/O error occurs
     * @throws ParseException if a topology object can not be created due to incorrect representation
     */
    @Throws(IOException::class, ParseException::class)
    fun parse(handler: TopologyParser.Handler) {
        parser.parse(KeyValueHandler(handler))
    }

    /**
     * Closes the stream and releases any system resources associated with it.
     */
    override fun close() {
        parser.close()
    }
}
