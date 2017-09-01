package io

import core.routing.NodeID
import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.Reader

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 */
class TopologyParser(reader: Reader, private val handler: TopologyParser.Handler): Closeable {

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

    /**
     * The underlying reader used to read the stream.
     */
    private val reader = BufferedReader(reader)

    /**
     * Returns a Topology object that is represented in the input source.
     *
     * @throws IOException    If an I/O error occurs
     * @throws ParseException if a topology object can not be created due to incorrect representation
     */
    @Throws(IOException::class, ParseException::class)
    fun parse() {

        // Read the first line - throw error if empty
        var line: String? = reader.readLine() ?: throw ParseException("Topology file is empty", lineNumber = 1)

        var currentLine = 1
        while (line != null) {

            // Do not parse blank lines
            if (!line.isBlank()) {
                parseLine(line, currentLine)
            }

            line = reader.readLine()
            currentLine++
        }
    }

    private fun parseLine(line: String, currentLine: Int) {

        // Each line must have a key separated from its values with an equal sign
        // e.g. node = 1

        // Split the key from the values
        val keyAndValues = line.split("=")

        if (keyAndValues.size != 2) {
            throw ParseException("Line $currentLine$ contains multiple equal signs(=): only one is permitted per line",
                    currentLine)
        }

        val key = keyAndValues[0].trim().toLowerCase()
        val values = keyAndValues[1].split("|").map { it.trim().toLowerCase() }.toList()

        when (key) {
            "node" -> {

                // Check there is at least one value: the node ID
                if (values.isEmpty() || (values.size == 1 && values[0].isEmpty())) {
                    throw ParseException("Line with `node` key is missing a value: must have at least an ID value",
                            currentLine)
                }

                // Node ID is the first value
                val nodeID = parseNodeID(values[0], currentLine)

                handler.onNodeItem(nodeID, values.subList(1, values.lastIndex + 1), currentLine)
            }
            "link" -> {

                // Check there is at least one value: the node ID
                if (values.size < 2) {
                    throw ParseException("Line with `link` is missing a value: must have at least two ID values",
                            currentLine)
                }

                // Node ID is the first value
                val tailID = parseNodeID(values[0], currentLine)
                val headID = parseNodeID(values[1], currentLine)

                handler.onLinkItem(tailID, headID, values.subList(2, values.lastIndex + 1), currentLine)
            }
            else -> {
                throw ParseException("Invalid key `$key`: keys must be either `node` or `link`", currentLine)
            }
        }
    }

    private fun parseNodeID(value: String, currentLine: Int): Int {

        try {
            val intValue = value.toInt()
            if (intValue < 0) {
                throw NumberFormatException()
            }

            return intValue

        } catch (e: NumberFormatException) {
            throw ParseException("Failed to parse node ID from value `$value`: must be a non-negative " +
                    "integer value", currentLine)
        }
    }

    /**
     * Closes the stream and releases any system resources associated with it.
     */
    override fun close() {
        reader.close()
    }
}
