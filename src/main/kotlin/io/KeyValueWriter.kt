package io

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

/**
 * Created on 09-11-2017
 *
 * @author David Fialho
 *
 * Writer that specializes in writing key/value pairs.
 * Each key/value pair is written to a new line.
 */
class KeyValueWriter(private val writer: BufferedWriter): AutoCloseable {

    /**
     * Helper constructor to create a KeyValueWriter from a File instance.
     */
    constructor(file: File): this(BufferedWriter(FileWriter(file)))

    /**
     * Writes a new key value pair.
     */
    fun write(key: Any, value: Any) {
        writer.apply {
            write("$key = $value")
            newLine()
        }
    }

    /**
     * Writes a new key value pair.
     */
    fun write(pair: Pair<Any, Any>) {
        writer.apply {
            write("${pair.first} = ${pair.second}")
            newLine()
        }
    }

    /**
     * Closes the underlying buffered writer.
     */
    override fun close() {
        writer.close()
    }
}