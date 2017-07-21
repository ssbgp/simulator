package bgp

import core.routing.Node
import core.routing.NodeID
import core.routing.Relationship

typealias BGPRelationship = Relationship<BGPNode, BGPRoute>

/**
 * Created on 20-07-2017
 *
 * @author David Fialho
 */
class BGPNode
internal constructor(id: NodeID, private val relationships: MutableList<BGPRelationship>) : Node(id) {

    /**
     * This method should be called when a message is received by the node.
     */
    fun onReceivingMessage(message: BGPMessage) {
        TODO("not implemented yet")
    }

    /**
     * Exports a route to the neighbor defined in the given relationship.
     */
    fun export(route: BGPRoute, relationship: BGPRelationship) {
        TODO("not implemented yet")
    }

    /**
     * Adds a relationship to this node.
     */
    fun addRelationship(neighbor: BGPNode, extender: BGPExtender) {
        relationships.add(BGPRelationship(neighbor, extender))
    }

    override fun toString(): String {
        return "BGPNode(id=$id)"
    }
}

//region Factory methods

/**
 * Returns a BGP node with the given ID an relationships. The relationships parameter is optional, if no value is
 * provided for it returns a BGPNode with no neighbors.
 *
 * @param id                        the ID to assign to the new node
 * @param relationships             a list containing all the relationships the node has (each one must be unique)
 * @throws IllegalArgumentException if the given relationships list contains any duplicate relationships
 */
@Throws(java.lang.IllegalArgumentException::class)
fun BGPNodeWith(id: NodeID, relationships: MutableList<Relationship<BGPNode, BGPRoute>> = ArrayList()): BGPNode {

    if (HashSet(relationships).size != relationships.size) {
        throw IllegalArgumentException("Can not create a BGP node with duplicate relationships")
    }

    return BGPNode(id, relationships)
}

//endregion