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
class BGPTopology(private val nodes: Map<NodeID, BGPNode>) : Topology<BGPNode, BGPRoute> {

    override val size: Int = nodes.size

    override fun getNode(id: NodeID): BGPNode? = nodes[id]

    override fun getNodes(): Collection<BGPNode> = nodes.values

    override fun getLinks(): Collection<Link<BGPNode, BGPRoute>> {

        val links = ArrayList<BGPLink>()

        for (node in nodes.values) {
            for ((neighbor, extender) in node.relationships) {
                links.add(BGPLink(neighbor, node, extender))
            }
        }

        return links
    }

    override fun nodeCount() = size

    override fun linkCount(): Int = nodes.flatMap { it.value.relationships }.count()

}

typealias BGPLink = Link<BGPNode, BGPRoute>
typealias BGPExtender = Extender<BGPRoute>