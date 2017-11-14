package io

import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.Reader

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 *
 * A parser for key-values formatted streams.
 *
 * A key-values formatted stream have multiple entries (one in each line) with the form:
 *
 *   key = value1 | value2 | ... | valueN
 *
 * The key is separated from the values with an equals sign '='.
 * A key can be associated with multiple values. Each one separated by a '|' character.
 *
 * Each parser is associated with an handler. This handler is notified every time a new entry is parsed from the stream.
 * The handler is then responsible for parsing the key and values, ensuring they are valid according to the required
 * specifications.
 *
 * Entries are parsed in the same order they are described in the stream. Thus, the handler is guaranteed to be
 * notified of new entries in that exact same order.
 */
class KeyValueParser(reader: Reader): Closeable {

    /**
     * Handlers are notified once a new key-value entry is parsed.
     *
     * Subclasses should use this method to parse the key and values, ensuring they are valid according to their
     * unique specifications.
     */
    interface Handler {

        /**
         * Invoked when a new entry is parsed.
         *
         * @param entry       the parsed entry
         * @param currentLine line number where the node was parsed
         */
        fun onEntry(entry: Entry, currentLine: Int)

    }

    data class Entry(val key: String, val values: List<String>)

    /**
     * The underlying reader used to read the stream.
     */
    private val reader = BufferedReader(reader)

    /**
     * Parses the stream, invoking the handler every time a new entry is parsed.
     *
     * @param handler the handler that is notified when a new entry is parsed
     * @throws IOException    If an I/O error occurs
     * @throws ParseException if the format of the stream is not valid
     */
    @Throws(IOException::class, ParseException::class)
    fun parse(handler: KeyValueParser.Handler) {

        // Read the first line - throw error if empty
        var line: String? = reader.readLine() ?: throw ParseException("file is empty", lineNumber = 1)

        var currentLine = 1
        while (line != null) {

            // Ignore blank lines
            if (!line.isBlank()) {
                val entry = parseEntry(line, currentLine)
                handler.onEntry(entry, currentLine)
            }

            line = reader.readLine()
            currentLine++
        }
    }

    private fun parseEntry(line: String, currentLine: Int): Entry {

        // Each line must have a key separated from its values with an equal sign
        // e.g. node = 1

        // Split the key from the values
        val keyAndValues = line.split("=", limit = 2)

        if (keyAndValues.size < 2) {
            throw ParseException("line $currentLine$ is missing an equal sign '=' to " +
                    "distinguish between key and values", currentLine)
        }

        val key = keyAndValues[0].trim()
        val values = keyAndValues[1].split("|").map { it.trim() }.toList()

        return Entry(key, values)
    }

    /**
     * Closes the stream and releases any system resources associated with it.
     */
    override fun close() {
        reader.close()
    }
}