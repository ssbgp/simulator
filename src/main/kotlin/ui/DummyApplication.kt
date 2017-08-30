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
object DummyApplication: Application {

    override fun launch(args: Array<String>) = Unit

    override fun loadTopology(topologyFile: File, topologyReader: TopologyReaderHandler,
                              loadBlock: () -> Topology<*>): Topology<*> = loadBlock()

    override fun findDestination(destinationID: NodeID, block: () -> Node<*>?): Node<*> = block()!!

    override fun execute(executionID: Int, destination: Node<*>, seed: Long, executeBlock: () -> Unit) = Unit

    override fun run(runBlock: () -> Unit) = Unit

}
