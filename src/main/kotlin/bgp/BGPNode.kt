package bgp

import core.routing.*
import core.simulator.Exporter
import core.simulator.ZeroDelayGenerator

typealias BGPRelationship = Relationship<BGPNode, BGPRoute>

/**
 * Created on 20-07-2017
 *
 * @author David Fialho
 */
class BGPNode private constructor(id: NodeID, val protocol: BaseBGPProtocol,
                                  private val exporter: Exporter) : Node(id) {
    /**
     * Defines a set of factory methods to create BGP nodes.
     */
    companion object Factory {

        /**
         * Returns a BGP node with the specified ID and with no neighbors.
         *
         * @param id the ID to assign to the new node
         */
        fun with(id: NodeID, protocol: BaseBGPProtocol = BGPProtocol(),
                 exporter: Exporter = Exporter(ZeroDelayGenerator())) = BGPNode(id, protocol, exporter)

    }

    /**
     * Mutable reference to the list containing the relationships this node holds.
     */
    private val mutableRelationships = ArrayList<BGPRelationship>()

    /**
     * Immutable reference to the relationships list. This gives public access to the relationships without providing
     * the ability to modify the list.
     */
    val relationships: List<BGPRelationship> get () = mutableRelationships

    /**
     * Routing table for this node. The table is wrapped in a Route Selector used to perform the route selection.
     */
    val routingTable = RouteSelector.wrapNewTable<BGPNode, BGPRoute>(BGPRoute.invalid(), ::bgpRouteCompare)

    /**
     * This method should be called when a message is received by the node.
     */
    fun onReceivingMessage(message: BGPMessage) {
        protocol.process(message)
    }

    /**
     * Exports the specified route to all neighbors of the node.
     *
     * TODO explain why we can export any route to all neighbors
     */
    fun export(route: BGPRoute) {

        for ((neighbor, extender) in relationships) {
            exporter.export(BGPMessage(sender = this, receiver = neighbor, extender = extender, route = route))
        }
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