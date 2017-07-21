package bgp

import core.routing.Extender
import core.routing.Link
import core.routing.NodeID
import core.routing.Topology

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 */
class BGPTopology(private val nodes: List<BGPNode>) : Topology<BGPNode, BGPRoute> {

    override val size: Int = nodes.size

    override fun getNode(id: Int): BGPNode {
        TODO("not implemented")
    }

    override fun getNodes(): Collection<BGPNode> = nodes

    override fun getLinks(): Collection<Link<BGPNode, BGPRoute>> {

        val links = ArrayList<BGPLink>()

        for (node in nodes) {
            for ((neighbor, extender) in node.relationships) {
                links.add(BGPLink(neighbor, node, extender))
            }
        }

        return links
    }

    override fun nodeCount(): Int = size

    override fun linkCount(): Int {

        val counter: Int = nodes
                .flatMap { it.relationships }
                .count()

        return counter
    }

}

typealias BGPLink = Link<BGPNode, BGPRoute>
typealias BGPExtender = Extender<BGPRoute>