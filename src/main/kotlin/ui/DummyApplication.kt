package ui

import core.routing.Node
import core.routing.NodeID
import core.routing.Route
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

    override fun <R: Route> loadTopology(topologyFile: File, topologyReader: TopologyReaderHandler<R>,
                              loadBlock: () -> Topology<R>): Topology<R> = loadBlock()

    override fun <R: Route> findDestination(destinationID: NodeID, block: () -> Node<R>?): Node<R> = block()!!

    override fun <R: Route> execute(executionID: Int, destination: Node<R>, seed: Long, executeBlock: () -> Unit) = Unit

    override fun run(runBlock: () -> Unit) = Unit

}
