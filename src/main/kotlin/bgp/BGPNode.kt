package bgp

import core.routing.*

typealias BGPRelationship = Relationship<BGPNode, BGPRoute>

/**
 * Created on 20-07-2017
 *
 * @author David Fialho
 */
class BGPNode internal constructor(id: NodeID, relationships: MutableList<BGPRelationship>) : Node(id) {

    /**
     * Mutable reference to the list containing the relationships this node holds.
     */
    private val mutableRelationships = relationships

    /**
     * Immutable reference to the relationships list. This gives public access to the relationships without providing
     * the ability to modify the list.
     */
    val relationships: List<BGPRelationship> get () = mutableRelationships

    /**
     * Routing table for this node. The table is wrapped in a Route Selector used to perform the route selection.
     */
    val routingTable = RouteSelector(
            table = RoutingTable(
                    invalidRoute = BGPRoute.invalid(),
                    neighbors = relationships.map { it.node }),
            forceReselect = false,
            compare = ::bgpRouteCompare
    )

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
        mutableRelationships.add(BGPRelationship(neighbor, extender))
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