package simulation

import core.simulator.Time

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 *
 * The BasicDataSet contains data collected by a basic collector that will be output by a basic reporter.
 *
 * @property delaySeed            the seed used to generate the message delays
 * @property messageCount         the total number o messages sent during the simulation
 * @property totalTerminationTime the time at which the last node exported its last route
 * @property avgTerminationTime   the average of the termination times of all nodes
 * @property detectionCount       the number of detections recorded during the simulation.
 * @property terminated           flag indicating if the simulation terminated or not
 * @property disconnectedCount     number of nodes left without a route when the simulation ended.
 */
data class BasicDataSet(
        var delaySeed: Long = 0L,
        var messageCount: Int = 0,
        var totalTerminationTime: Time = 0,
        var avgTerminationTime: Double = 0.0,
        var detectionCount: Int = 0,
        var terminated: Boolean = true,
        var disconnectedCount: Int = 0

): DataSet {

    /**
     * Clears all data from the data set.
     */
    override fun clear() {
        delaySeed = 0L
        messageCount = 0
        totalTerminationTime = 0
        avgTerminationTime = 0.0
        detectionCount = 0
        terminated = true
        disconnectedCount = 0
    }
}