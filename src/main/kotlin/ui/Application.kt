package ui

import core.routing.Node
import core.routing.NodeID
import core.routing.Topology
import io.TopologyReaderHandler
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
     * @param topologyFile   the file from which the topology will be loaded
     * @param topologyReader the reader used to load the topology into memory
     * @param loadBlock      the code block to load the topology.
     */
    fun loadTopology(topologyFile: File, topologyReader: TopologyReaderHandler,
                     loadBlock: () -> Topology<*>): Topology<*>

    fun findDestination(destinationID: NodeID, block: () -> Node<*>?): Node<*>

    /**
     * Invoked while executing each execution.
     *
     * @param executionID  the identifier of the execution
     * @param destination  the destination used in the execution
     * @param seed         the seed of the message delay generator used for the execution
     * @param executeBlock the code block that performs one execution
     */
    fun execute(executionID: Int, destination: Node<*>, seed: Long, executeBlock: () -> Unit)

    /**
     * Invoked during a run.
     */
    fun run(runBlock: () -> Unit)

}