package simulation

import bgp.notifications.*
import core.simulator.notifications.BasicNotifier
import core.simulator.notifications.StartListener
import core.simulator.notifications.StartNotification
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

/**
 * Created on 15-11-2017
 *
 * @author David Fialho
 *
 * TODO @doc
 * TODO @optimization - try different methods of writing that may speedup the simulation process
 * TODO @refactor - print much nicer messages for each event!!
 */
class TraceReporter(outputFile: File): DataCollector, StartListener,
        LearnListener, ExportListener, SelectListener, DetectListener {

    private val baseOutputFile = outputFile

    /**
     * The reporter outputs the trace of each simulation to its own file.
     * This variable stores the output file for the current simulation. It is updated every time a new simulation
     * starts.
     */
    private var simulationWriter: BufferedWriter? = null
    private var simulationNumber = 0

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
        simulationWriter?.close()
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
        simulationNumber++

        val simulationOutputFile = File(baseOutputFile.parent, baseOutputFile.nameWithoutExtension +
                "$simulationNumber.${baseOutputFile.extension}")

        simulationWriter?.close()
        simulationWriter = BufferedWriter(FileWriter(simulationOutputFile))
    }

    /**
     * Invoked to notify the listener of a new learn notification.
     */
    override fun notify(notification: LearnNotification) {
        simulationWriter?.apply {
            write("${notification.time}: $notification\n")
        }
    }

    /**
     * Invoked to notify the listener of a new export notification.
     */
    override fun notify(notification: ExportNotification) {
        simulationWriter?.apply {
            write("${notification.time}: $notification\n")
        }
    }

    /**
     * Invoked to notify the listener of a new learn notification.
     */
    override fun notify(notification: SelectNotification) {
        simulationWriter?.apply {
            write("${notification.time}: $notification\n")
        }
    }

    /**
     * Invoked to notify the listener of a new detect notification.
     */
    override fun notify(notification: DetectNotification) {
        simulationWriter?.apply {
            write("${notification.time}: $notification\n")
        }
    }

}