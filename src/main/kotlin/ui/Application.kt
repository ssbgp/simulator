package ui

import core.routing.Node
import core.routing.NodeID
import core.routing.Route
import core.routing.Topology
import core.simulator.Advertisement
import java.io.File

/**
 * Created on 30-08-2017
 *
 * @author David Fialho
 */
interface Application {

    fun launch(args: Array<String>)

    /**
     * Invoked while loading the topology.
     *
     * @param topologyFile the file from which the topology will be loaded
     * @param block        the code block to load the topology
     */
    fun <R: Route> loadTopology(topologyFile: File, block: () -> Topology<R>): Topology<R>

    /**
     * Invoked when determining which nodes will be advertisers. Some of these nodes may be
     * stubs, which implies accessing the filesystem, which may throw some IO error.
     *
     * @param ids   the IDs of the advertising nodes
     * @param block the block of code to find the advertising nodes
     * @return a list containing the advertisers found
     */
    fun <R: Route> findAdvertisers(ids: List<NodeID>, block: () -> List<Node<R>>): List<Node<R>>

    /**
     * Invoked while executing each execution.
     *
     * @param executionID    the identifier of the execution
     * @param advertisements the advertisements programmed to occur in the simulation
     * @param seed           the seed of the message delay generator used for the execution
     * @param block          the code block that performs one execution
     */
    fun <R: Route> execute(executionID: Int, advertisements: List<Advertisement<R>>, seed: Long,
                           block: () -> Unit)

    /**
     * Invoked during a run.
     */
    fun run(runBlock: () -> Unit)

}