package simulation

import bgp.notifications.*
import core.routing.NodeID
import core.simulator.Time
import core.simulator.notifications.*
import io.BasicReporter
import java.io.File
import java.io.IOException

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 *
 * The BasicDataCollector collects the most `basic` information from a simulation execution.
 * The collected data is stored in a BasicDataSet and reported using a BasicReporter.
 *
 * @param reporter the reporter used to report the final data.
 */
class BasicDataCollector(private val reporter: BasicReporter) : DataCollector,
        StartListener,
        MessageSentListener,
        ExportListener,
        DetectListener,
        ThresholdReachedListener,
        EndListener {

    /**
     * Creates a Basic Reporter that will output results to the specified output file.
     */
    constructor(outputFile: File) : this(BasicReporter(outputFile))

    /**
     * Stores the final data to be reported.
     */
    val data = BasicDataSet()

    /**
     * Stores the number of nodes contained in the topology that was simulated.
     */
    private var nodeCount = 0

    /**
     * Keeps record of the termination times of each node. The termination time of a node corresponds to the time at
     * which the node exported its last route.
     *
     * This information will be used to compute the average termination time.
     */
    private val terminationTimes = HashMap<NodeID, Time>()

    /**
     * Adds the collector as a listener for notifications the collector needs to listen to collect data.
     */
    override fun register() {
        Notifier.addStartListener(this)
        Notifier.addMessageSentListener(this)
        BGPNotifier.addExportListener(this)
        BGPNotifier.addDetectListener(this)
        Notifier.addThresholdReachedListener(this)
        Notifier.addEndListener(this)
    }

    /**
     * Removes the collector from all notifiers
     */
    override fun unregister() {
        Notifier.removeStartListener(this)
        Notifier.removeMessageSentListener(this)
        BGPNotifier.removeExportListener(this)
        BGPNotifier.removeDetectListener(this)
        Notifier.removeThresholdReachedListener(this)
        Notifier.removeEndListener(this)
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
        terminationTimes.clear()
    }

    // region Notify methods used to collect data

    /**
     * Invoked to notify the listener of a new start notification.
     */
    override fun onStart(notification: StartNotification) {
        data.delaySeed = notification.seed
        nodeCount = notification.topology.size
    }

    /**
     * Invoked to notify the listener of a new message sent notification.
     */
    override fun onMessageSent(notification: MessageSentNotification) {
        data.messageCount++
        data.totalTerminationTime = notification.time
    }

    /**
     * Invoked to notify the listener of a new export notification.
     */
    override fun onExport(notification: ExportNotification) {
        terminationTimes[notification.node.id] = notification.time
    }

    /**
     * Invoked to notify the listener of a new detect notification.
     */
    override fun onDetect(notification: DetectNotification) {
        data.detectionCount++
    }

    /**
     * Invoked to notify the listener of a new threshold reached notification.
     */
    override fun onThresholdReached(notification: ThresholdReachedNotification) {
        data.terminated = false
    }

    /**
     * Invoked to notify the listener of a new end notification.
     */
    override fun onEnd(notification: EndNotification) {

        // Count the nodes that were left disconnected when the simulation ended
        data.disconnectedCount = notification.topology.nodes
                .filterNot { it.protocol.selectedRoute.isValid() }
                .count()

        // If a node never exports a route then it will not be included in [terminationTimes]. In that case the
        // termination time of that node is 0

        // The average termination time corresponds to the mean of the termination times of all nodes
        data.avgTerminationTime = terminationTimes.values.sum().div(nodeCount.toDouble())
    }

    // endregion
}