package simulation

import core.routing.Node
import core.routing.Route
import core.routing.Topology
import core.simulator.Time

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 */
interface Execution<R: Route> {

    /**
     * Performs a single simulation execution with the specified topology and destination.
     */
    fun execute(topology: Topology<R>, destination: Node<R>, threshold: Time)

}