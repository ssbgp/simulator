package bgp

import core.routing.NodeID

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 */
class BGPTopologyBuilder {

    private data class Link(val tailID: NodeID, val headID: NodeID, val extender: BGPExtender)

    private val ids = HashMap<NodeID, BaseBGPProtocol>()
    private val links = mutableSetOf<Link>()

    /**
     * Adds a new node with the specified ID to the builder. If a node with the same ID was already added to the
     * builder then it does not add anything.
     *
     * It provides the option to specify the protocol to associate with the new node. If none is provided then it
     * will use the BGPProtocol by default.
     *
     * @param id       the ID to identify the new node
     * @param protocol the protocol to associate with the new node
     * @return true if the ID was not added yet to the builder or false if otherwise
     */
    fun  addNode(id: NodeID, protocol: BaseBGPProtocol = BGPProtocol()): Boolean {
        return ids.putIfAbsent(id, protocol) == null
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
        for ((id, protocol) in ids) nodes.put(id, BGPNode.with(id, protocol))

        // Establish relationships based on the links stored in the builder
        for ((tail, head, extender) in links) {
            nodes[head]?.addRelationship(nodes[tail]!!, extender)
        }

        return BGPTopology(nodes)
    }

}