package io

import bgp.BGPRoute
import simulation.NodeDataSet
import java.io.File
import java.io.IOException

/**
 * Created on 02-10-2017.
 *
 * @author David Fialho
 *
 * IMPORTANT: This Reporter only works for BGP routes!!
 */
class NodeDataReporter(private val outputFile: File): Reporter<NodeDataSet> {

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
     */
    @Throws(IOException::class)
    override fun report(data: NodeDataSet) {

        CSVPrinter(outputFile).use {

            if (!wereHeadersPrinted) {
                it.printRecord(
                        "Simulation",
                        "Node",
                        "Local Preference",
                        "Next-hop",
                        "Path Length",
                        "Termination Time"
                )

                wereHeadersPrinted = true
            }

            for ((nodeID, selectedRoute) in data.selectedRoutes) {

                selectedRoute as BGPRoute

                it.printRecord(
                        simulation,
                        nodeID,
                        selectedRoute.localPref.toInterdomainLabel(),
                        selectedRoute.asPath.nextHop()?.id,
                        selectedRoute.asPath.size,
                        data.terminationTimes[nodeID]
                )
            }

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