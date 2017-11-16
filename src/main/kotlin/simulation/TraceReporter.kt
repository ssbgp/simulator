package simulation

import bgp.BGPRoute
import bgp.notifications.*
import bgp.policies.interdomain.*
import core.routing.Node
import core.routing.Path
import core.routing.Route
import core.simulator.notifications.BasicNotifier
import core.simulator.notifications.StartListener
import core.simulator.notifications.StartNotification
import java.io.BufferedWriter
import java.io.Closeable
import java.io.File
import java.io.FileWriter

/**
 * Created on 15-11-2017
 *
 * @author David Fialho
 *
 * TODO @doc
 * TODO @optimization - try different methods of writing that may speedup the simulation process
 */
class TraceReporter(outputFile: File): DataCollector, StartListener,
        LearnListener, ExportListener, SelectListener, DetectListener, Closeable {

    private val baseOutputFile = outputFile

    /**
     * The reporter outputs the trace of each simulation to its own file.
     * This variable stores the output file for the current simulation. It is updated every time a new simulation
     * starts.
     */
    private var simulationWriter: BufferedWriter? = null
    private var simulationNumber = 0

    /**
     * Stores the size for the "Node" column. This depends on the size of longest node ID.
     * By default, it is set to fit the word "Node" included in the header.
     */
    private var nodeColumnSize = 4

            /**
     * Adds the collector as a listener for notifications the collector needs to listen to collect data.
     */
    override fun register() {
        BasicNotifier.addStartListener(this)
        BGPNotifier.addLearnListener(this)
        BGPNotifier.addExportListener(this)
        BGPNotifier.addSelectListener(this)
        BGPNotifier.addDetectListener(this)
    }

    /**
     * Removes the collector from all notifiers
     */
    override fun unregister() {
        BasicNotifier.removeStartListener(this)
        BGPNotifier.removeLearnListener(this)
        BGPNotifier.removeExportListener(this)
        BGPNotifier.removeSelectListener(this)
        BGPNotifier.removeDetectListener(this)

        // Unregister must always be called before discarding the trace reporter
        // Thus, it is a go way to ensure that the current writer is closed to
        close()
    }

    /**
     * Closes the underlying writer.
     */
    override fun close() {
        simulationWriter?.close()
        simulationWriter = null
    }

    /**
     * Reports the currently collected data.
     */
    override fun report() {
        // nothing to do
        // reporting is done during the execution
    }

    /**
     * Clears all collected data.
     */
    override fun clear() {
        // nothing to do
    }

    /**
     * Invoked to notify the listener of a new start notification.
     */
    override fun notify(notification: StartNotification) {
        // Keeping track of the number of simulations is important to ensure that the tracing output from a new
        // simulation does not overwrite the previous one
        simulationNumber++

        // The trace output of each simulation is written to its own file
        val simulationOutputFile = File(baseOutputFile.parent, baseOutputFile.nameWithoutExtension +
                "$simulationNumber.${baseOutputFile.extension}")

        // Close writer used for a previous simulation and create a new one for the new simulation
        simulationWriter?.close()
        simulationWriter = BufferedWriter(FileWriter(simulationOutputFile))

        // Look for all node IDs to determine which one has the longest ID number
        val maxIDSize = notification.topology.nodes.asSequence().map { it.id }.max() ?: 0

        // Node column size corresponds to longest between the word "Node" and the longest node ID
        nodeColumnSize = maxOf(4, maxIDSize)

        // Write headers
        simulationWriter?.apply {
            write("${align("Time")}| Event  | ${align("Node", nodeColumnSize)} | Routing Information\n")
        }
    }

    /**
     * Invoked to notify the listener of a new learn notification.
     */
    override fun notify(notification: LearnNotification) {
        simulationWriter?.apply {
            notification.apply {
                write("${align(time)}| LEARN  | ${align(node.pretty(), nodeColumnSize)} | ${route.pretty()} " +
                        "via ${neighbor.pretty()}\n")
            }
        }
    }

    /**
     * Invoked to notify the listener of a new export notification.
     */
    override fun notify(notification: ExportNotification) {
        simulationWriter?.apply {
            notification.apply {
                write("${align(time)}| EXPORT | ${align(node.pretty(), nodeColumnSize)} | ${route.pretty()}\n")
            }
        }
    }

    /**
     * Invoked to notify the listener of a new learn notification.
     */
    override fun notify(notification: SelectNotification) {
        simulationWriter?.apply {
            notification.apply {
                write("${align(time)}| SELECT | ${align(node.pretty(), nodeColumnSize)} | " +
                        "${selectedRoute.pretty()} over ${previousRoute.pretty()}\n")
            }
        }
    }

    /**
     * Invoked to notify the listener of a new detect notification.
     */
    override fun notify(notification: DetectNotification) {
        simulationWriter?.apply {
            notification.apply {
                write("${align(time)}| DETECT | ${align(node.pretty(), nodeColumnSize)} |\n")
            }
        }
    }

    //
    //  Helper functions to prettify some objects
    //

    private fun <R: Route> Node<R>.pretty(): String = id.toString()

    private fun Path.pretty(): String = joinToString(transform = {it.pretty()})

    private fun BGPRoute.pretty(): String {

        if (this === BGPRoute.invalid() || this === BGPRoute.self()) {
            return toString()
        }

        val cost = when (localPref) {
            LOCAL_PREF_PEERPLUS -> "r+"
            LOCAL_PREF_PEERSTAR -> "r*"
            LOCAL_PREF_CUSTOMER -> "c"
            LOCAL_PREF_PEER     -> "r"
            LOCAL_PREF_PROVIDER -> "p"
            else -> localPref.toString()
        }

        return "($cost, ${asPath.pretty()})"
    }

    //
    //  Helper functions to help align the information shown in the messages
    //

    private fun align(value: Any, length: Int = 7): String {

        val builder = StringBuilder(length)

        val text = value.toString()
        val remainder = length - text.length
        val padding = remainder / 2

        // Add padding to the left
        for (i in 1..(padding))
            builder.append(' ')

        // Add the text at the center
        builder.append(text)

        // Add padding to the right
        for (i in 1..(padding + remainder % 2))
            builder.append(' ')

        return builder.toString()
    }

}