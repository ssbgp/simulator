package bgp

import bgp.notifications.BGPNotifier
import bgp.notifications.MessageReceivedNotification
import bgp.notifications.MessageSentNotification
import core.routing.*
import core.simulator.DelayGenerator
import core.simulator.Engine
import core.simulator.Exporter
import core.simulator.ZeroDelayGenerator

typealias BGPRelationship = Relationship<BGPNode, BGPRoute>

/**
 * Created on 20-07-2017
 *
 * @author David Fialho
 */
class BGPNode private constructor(id: NodeID, val protocol: BaseBGPProtocol) : Node(id), Destination {

    /**
     * Defines a set of factory methods to create BGP nodes.
     */
    companion object Factory {

        /**
         * Returns a BGP node with the specified ID and with no neighbors.
         *
         * @param id the ID to assign to the new node
         * @param protocol the protocol implemented by the node
         */
        fun with(id: NodeID, protocol: BaseBGPProtocol = BGPProtocol()) = BGPNode(id, protocol)

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
        BGPNotifier.notifyMessageReceived(MessageReceivedNotification(Engine.scheduler.time, message))
        protocol.process(message)
    }

    /**
     * Exports the specified route to all neighbors of the node.
     *
     * TODO explain why we can export any route to all neighbors
     */
    fun export(route: BGPRoute) {

        for ((neighbor, extender, exporter) in relationships) {
            val message = BGPMessage(sender = this, receiver = neighbor, extender = extender, route = route)
            exporter.export(message)

            BGPNotifier.notifyMessageSent(MessageSentNotification(Engine.scheduler.time, message))
        }
    }

    /**
     * Announces a BGP self route to all neighbors of this node.
     */
    override fun announceItSelf() {
        val selfRoute = BGPRoute.self()
        routingTable.update(this, selfRoute)
        export(selfRoute)
    }

    /**
     * Adds a relationship from this node to the specified neighbor.
     *
     * @param neighbor       the neighbor to create relationship with
     * @param extender       the extender that models the routes exported from this node to the specified neighbor
     * @param delayGenerator the generator that generates the message delays from this node to the specified neighbor
     */
    fun addRelationship(neighbor: BGPNode, extender: BGPExtender, delayGenerator: DelayGenerator = ZeroDelayGenerator) {
        mutableRelationships.add(BGPRelationship(neighbor, extender, Exporter(delayGenerator)))
    }

    /**
     * Resets the state of this node as if it was just created.
     * It keeps the relationships. It just resets the state related to routing.
     */
    fun reset() {
        routingTable.clear()
        protocol.reset()
    }

    override fun toString(): String {
        return "BGPNode(id=$id)"
    }
}