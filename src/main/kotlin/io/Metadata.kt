package io

import core.simulator.Time
import java.io.File
import java.io.FileWriter
import java.io.Writer
import java.time.Instant

/**
 * Created on 27-10-2017
 *
 * @author David Fialho
 *
 * Data class to hold metadata information.
 * It also provides methods to output the information to a file or a stream.
 */
data class Metadata(
        val version: String,
        val startInstant: Instant,
        val finishInstant: Instant,
        val topologyFilename: String,
        val stubsFilename: String?,
        val destinationID: Int,
        val minDelay: Time,
        val maxDelay: Time,
        val threshold: Time
) {

    /**
     * Prints metadata information using the specified writer.
     */
    fun print(writer: Writer) {

        writer.apply {
            write("version = $version\n")
            write("start datetime = $startInstant\n")
            write("finish datetime = $finishInstant\n")
            write("topology file = $topologyFilename\n")
            if (stubsFilename != null) write("stubs file = $stubsFilename\n")
            write("destination = $destinationID\n")
            write("min delay = $minDelay\n")
            write("max delay = $maxDelay\n")
            write("threshold = $threshold\n")
        }
    }

    /**
     * Prints metadata information to the specified file.
     */
    fun print(outputFile: File) {
        FileWriter(outputFile).use {
            print(it)
        }
    }

}
