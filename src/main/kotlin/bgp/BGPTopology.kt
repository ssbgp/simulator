package bgp

import core.routing.Topology

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 */
class BGPTopology(private val nodes: List<BGPNode>) : Topology<BGPNode> {

    override val size: Int = nodes.size

    override fun getNode(id: Int): BGPNode {
        TODO("not implemented")
    }

    override fun getNodes(): Collection<BGPNode> = nodes

    override fun nodeCount(): Int {
        TODO("not implemented")
    }

    override fun linkCount(): Int {
        TODO("not implemented")
    }

}