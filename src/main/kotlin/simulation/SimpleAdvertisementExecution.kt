package simulation

import core.routing.Node
import core.routing.Topology
import core.simulator.Engine
import core.simulator.Time

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 */
class SimpleAdvertisementExecution(val threshold: Time): Execution {

    val dataCollectors = DataCollectorGroup()

    override fun execute(topology: Topology<*>, destination: Node<*>) {

        dataCollectors.clear()

        val data = dataCollectors.collect {
            Engine.simulate(topology, destination, threshold)
        }

        data.processData()
        data.report()
    }

}