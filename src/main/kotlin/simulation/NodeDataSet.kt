package simulation

import core.routing.NodeID
import core.routing.Route
import core.simulator.Time

/**
 * Created on 02-10-2017.
 *
 * @author David Fialho
 */
class NodeDataSet : DataSet {

    /**
     * Data stored for each node.
     */
    data class NodeData(var selectedRoute: Route, var terminationTime: Time = 0)

    val terminationTimes: MutableMap<NodeID, Time> = HashMap()
    val selectedRoutes: MutableMap<NodeID, Route> = HashMap()

    /**
     * Clears all data from the data set.
     */
    override fun clear() {
        terminationTimes.clear()
        selectedRoutes.clear()
    }
}