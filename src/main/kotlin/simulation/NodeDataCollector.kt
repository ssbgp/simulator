package simulation

import bgp.notifications.BGPNotifier
import bgp.notifications.ExportListener
import bgp.notifications.ExportNotification
import core.simulator.notifications.*
import io.NodeDataReporter
import java.io.File
import java.io.IOException

/**
 * Created on 01-10-2017.
 *
 * @author David Fialho
 *
 * The node data collector collects data relative to each individual node in the topology.
 */
class NodeDataCollector(private val reporter: NodeDataReporter) :
        DataCollector, StartListener, EndListener, ExportListener {

    /**
     * Creates a Basic Reporter that will output results to the specified output file.
     */
    constructor(outputFile: File) : this(NodeDataReporter(outputFile))

    private val data = NodeDataSet()

    /**
     * Adds the collector as a listener for notifications the collector needs to listen to collect data.
     */
    override fun register() {
        Notifier.addStartListener(this)
        Notifier.addEndListener(this)
        BGPNotifier.addExportListener(this)
    }

    /**
     * Removes the collector from all notifiers
     */
    override fun unregister() {
        Notifier.removeStartListener(this)
        Notifier.removeEndListener(this)
        BGPNotifier.removeExportListener(this)
    }

    /**
     * Reports the currently collected data.
     *
     * @throws IOException If an I/O error occurs
     */
    @Throws(IOException::class)
    override fun report() {
        reporter.report(data)
    }

    /**
     * Clears all collected data.
     */
    override fun clear() {
        data.clear()
    }

    /**
     * Invoked to notify the listener of a new start notification.
     */
    override fun onStart(notification: StartNotification) {
        // Ensure that all nodes start with a termination time of 0. This also ensures
        // that all nodes are included in the terminationTimes map Why is this necessary?
        // It may occur the some nodes never export a route. If that is the case, then
        // these nodes would not be included in the terminationTimes map.
        for (node in notification.topology.nodes)
            data.terminationTimes[node.id] = 0
    }

    /**
     * Invoked to notify the listener of a new export notification.
     */
    override fun onExport(notification: ExportNotification) {
        // Update termination time of the node that exported a new route
        data.terminationTimes[notification.node.id] = notification.time
    }

    /**
     * Invoked to notify the listener of a new end notification.
     */
    override fun onEnd(notification: EndNotification) {
        for (node in notification.topology.nodes)
            data.selectedRoutes[node.id] = node.protocol.selectedRoute
    }

}