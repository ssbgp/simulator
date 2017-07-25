package bgp

import core.routing.NodeID

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 */
class BGPTopologyBuilder {

    private data class Link(val tailID: NodeID, val headID: NodeID, val extender: BGPExtender)

    private val ids = mutableSetOf<NodeID>()
    private val links = mutableSetOf<Link>()

    /**
     * Adds a new node with the specified ID to the builder. If a node with the same ID was already added to the
     * builder then it does not add anything.
     *
     * @return true if the ID was not added yet to the builder or false if otherwise
     */
    fun  addNode(id: NodeID): Boolean {
        return ids.add(id)
    }

    /**
     * Adds a new link connecting the node identified by the 'from' ID with to the node identified by the 'to' ID. It
     * associates the link the specified extender that will be used to extend the routes learned by the 'from' node
     * from the 'to' node.
     */
    fun addLink(from: NodeID, to: NodeID, extender: BGPExtender): Boolean {

        if (from !in ids || to !in ids) {
            return false
        }

        return links.add(Link(from, to, extender))
    }

    /**
     * Returns a BGPTopology containing the nodes and relationships defined in the builder at the time the method is
     * called.
     */
    fun build(): BGPTopology {

        // Data structure that will hold the nodes
        val nodes = HashMap<NodeID, BGPNode>(ids.size)

        // Create a node for each ID
        for (it in ids) nodes.put(it, BGPNode.with(it))

        // Establish relationships based on the links stored in the builder
        for ((tail, head, extender) in links) {
            nodes[head]?.addRelationship(nodes[tail]!!, extender)
        }

        return BGPTopology(nodes)
    }

}