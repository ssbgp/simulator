package bgp

import core.routing.Extender
import core.routing.Link
import core.routing.NodeID
import core.routing.Topology

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 *
 * A BGP topology connects BGPNodes that use a BGP like protocol to communicate. Nodes participating in this kind of
 * protocols exchange BGPRoutes between each other.
 */
class BGPTopology(private val nodes: Map<NodeID, BGPNode>) : Topology<BGPNode, BGPRoute> {

    /**
     * Returns the number of BGP nodes in the topology.
     */
    override val size: Int = nodes.size

    /**
     * Returns the number of links in the topology.
     *
     * Due to the way links are stored in this type of topology, this is a time consuming operation. Try to avoid
     * having to use it.
     */
    override val linkCount: Int
        get() = nodes.flatMap { it.value.relationships }.count()

    /**
     * Returns the BGP identified by the specified ID. It returns null if the topology does not hold any node with
     * the specified ID.
     */
    override operator fun get(id: NodeID): BGPNode? = nodes[id]

    /**
     * Returns an immutable collection containing all nodes in the topology.
     */
    override fun getNodes(): Collection<BGPNode> = nodes.values

    /**
     * Returns a collection containing all links between nodes in the topology.
     *
     * Due to the way links are stored in this type of topology, this is a time consuming operation. Try to avoid
     * having to use it.
     */
    override fun getLinks(): Collection<Link<BGPNode, BGPRoute>> {

        val links = ArrayList<BGPLink>()

        for (node in nodes.values) {
            for ((neighbor, extender) in node.relationships) {
                links.add(BGPLink(neighbor, node, extender))
            }
        }

        return links
    }

}

typealias BGPLink = Link<BGPNode, BGPRoute>
typealias BGPExtender = Extender<BGPNode, BGPRoute>