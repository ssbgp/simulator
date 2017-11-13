package io

import core.routing.NodeID
import utils.toNonNegativeInt
import java.io.*

/**
 * Created on 31-08-2017
 *
 * @author David Fialho
 */
class StubParser(reader: Reader): Closeable {

    /**
     * Creates a stub parser with a file reader to parse a file.
     *
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for
     * some other reason cannot be opened for reading.
     */
    @Throws(FileNotFoundException::class)
    constructor(stubFile: File): this(FileReader(stubFile))

    /**
     * Interface for an handler that is called when a new stub item is parsed.
     */
    interface Handler {

        /**
         * Invoked when a new stub item is found.
         *
         * @param id          the ID of the stub
         * @param inNeighbor  the ID of the stub's in-neighbor
         * @param label       the label of the extender associated with the link between the neighbor and the stub
         * @param currentLine line number where the stub link was parsed
         */
        fun onStubLink(id: NodeID, inNeighbor: NodeID, label: String, currentLine: Int)

    }

    private val reader = BufferedReader(reader)

    /**
     * Parses the stub file and notifies the handler once a new stub is parsed.
     *
     * @param handler the handler that will be notified of new stub items
     * @throws IOException    If an I/O error occurs
     * @throws ParseException If a parse error occurs
     */
    @Throws(IOException::class, ParseException::class)
    fun parse(handler: Handler) {

        var line: String? = reader.readLine() ?: return
        var currentLine = 1

        while (line != null) {

            // Ignore blank lines
            if (!line.isBlank())
                parseLine(line, handler, currentLine)

            line = reader.readLine()
            currentLine++
        }

    }

    private fun parseLine(line: String, handler: Handler, currentLine: Int) {

        val values = line.split("|").map { it.trim() }

        if (values.size != 3) {
            throw ParseException("A stub item requires 3 values, but ${values.size} were provided", currentLine)
        }

        val stubID = try {
            values[0].toNonNegativeInt()
        } catch (e: NumberFormatException) {
            throw ParseException("Stub ID must be non-negative integer number: was `${values[0]}`", currentLine)
        }

        val inNeighborID = try {
            values[1].toNonNegativeInt()
        } catch (e: NumberFormatException) {
            throw ParseException("In-neighbor ID must be non-negative integer number: was `${values[1]}`", currentLine)
        }

        val label = values[2]
        if (label.isBlank()) throw ParseException("Stub item is missing label value", currentLine)

        handler.onStubLink(stubID, inNeighborID, label, currentLine)
    }

    /**
     * Resets the input stream.
     *
     * @throws IOException  If the stream does not support reset(), or if some other I/O error occurs
     */
    @Throws(IOException::class)
    fun reset() {
        if (!reader.markSupported())
            reader.reset()
    }

    /**
     * Closes the input stream.
     */
    override fun close() {
        reader.close()
    }
}