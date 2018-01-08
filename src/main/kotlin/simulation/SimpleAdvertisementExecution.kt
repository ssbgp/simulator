package simulation

import core.routing.Route
import core.routing.Topology
import core.simulator.Advertisement
import core.simulator.Simulator
import core.simulator.Time
import java.io.IOException

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 */
class SimpleAdvertisementExecution<R: Route>: Execution<R> {

    val dataCollectors = DataCollectorGroup()

    /**
     * Executes a simulation, collects data from it, and reports the results.
     *
     * To collect data, before calling this method the data collectors to be used must be specified,
     * by adding each collector to the data collector group of this execution.
     *
     * @throws IOException If an I/O error occurs
     */
    @Throws(IOException::class)
    override fun execute(topology: Topology<R>, advertisement: Advertisement<R>, threshold: Time) {
        execute(topology, listOf(advertisement), threshold)
    }

    /**
     * Executes a simulation, collects data from it, and reports the results.
     *
     * To collect data, before calling this method the data collectors to be used must be specified,
     * by adding each collector to the data collector group of this execution.
     *
     * @throws IOException If an I/O error occurs
     */
    @Throws(IOException::class)
    override fun execute(topology: Topology<R>, advertisements: List<Advertisement<R>>,
                         threshold: Time) {

        dataCollectors.clear()

        val data = dataCollectors.collect {
            Simulator.simulate(topology, advertisements, threshold)
        }

        data.report()
    }
}