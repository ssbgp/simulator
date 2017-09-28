package io

import simulation.BasicDataSet
import java.io.File
import java.io.IOException


/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 *
 * Reporter used to reporter data from a basic data set.
 */
class BasicReporter(private val outputFile: File): Reporter<BasicDataSet> {

    /**
     * Flag to indicate if the headers were already printed.
     * Helps to ensure the headers are printed only once despite the report() method being called multiple times.
     */
    private var wereHeadersPrinted = false

    /**
     * Stores the simulation number, Incremented each time the report() method is called.
     */
    private var simulation = 1

    /**
     * Reports a data set.
     *
     * @throws IOException If an I/O error occurs
     */
    @Throws(IOException::class)
    override fun report(data: BasicDataSet) {

        CSVPrinter(outputFile).use {

            if (!wereHeadersPrinted) {
                it.printRecord(
                        "Simulation",
                        "Delay Seed",
                        "Termination Time (Total)",
                        "Termination Time (Avg.)",
                        "Message Count",
                        "Detection Count",
                        "Terminated",
                        "Disconnected Count"
                )

                wereHeadersPrinted = true
            }

            it.printRecord(
                    simulation,
                    data.delaySeed,
                    data.totalTerminationTime,
                    data.avgTerminationTime,
                    data.messageCount,
                    data.detectionCount,
                    if (data.terminated) "Yes" else "No",
                    data.disconnectedCount
            )

            simulation++
        }
    }

    /**
     * Resets the reporter to its initial state.
     */
    override fun reset() {
        wereHeadersPrinted = false
        simulation = 1
    }

}