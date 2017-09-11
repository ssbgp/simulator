package testing

import bgp.BGPRoute
import bgp.BaseBGP
import bgp.policies.interdomain.*
import bgp.policies.shortestpath.ShortestPathExtender
import core.routing.*

object DummyBGPExtender : Extender<BGPRoute> {
    override fun extend(route: BGPRoute, sender: Node<BGPRoute>): BGPRoute = route
}

/**
 * Entry point to start creating a topology that uses a BGP like protocol.
 */
fun bgpTopology(body: TopologyBuilder<BGPRoute>.() -> Unit): Topology<BGPRoute> {

    val builder = TopologyBuilder<BGPRoute>()
    body(builder)
    return builder.build()
}

/**
 * Entry point to declare a node in the topology.
 */
fun TopologyBuilder<BGPRoute>.node(nodePair: () -> Pair<Int, BaseBGP>) {

    val pair = nodePair()
    this.addNode(id = pair.first, protocol = pair.second)
}

/**
 * Set that a node is deploying the specified protocol.
 */
infix fun Int.deploying(protocol: BaseBGP): Pair<Int, BaseBGP> {
    return Pair(this, protocol)
}

/**
 * Entry point to declare a link in the topology.
 */
fun TopologyBuilder<BGPRoute>.link(createLink: () -> TemporaryLink) {

    val link = createLink()
    this.link(link.tail, link.head, link.extender)
}

/**
 * Data structure characterizing a link.
 */
data class TemporaryLink(val tail: NodeID, val head: NodeID, var extender: Extender<BGPRoute> = DummyBGPExtender)

/**
 * Connector used to indicate the head of the link.
 */
infix fun Int.to(head: Int) = TemporaryLink(tail = this, head = head)

/**
 * Connector used to specify the extender of the link
 */
infix fun TemporaryLink.using(extender: Extender<BGPRoute>): TemporaryLink {
    this.extender = extender
    return this
}

//region Shortest Path Routing

/**
 * Connector used to specify the cost of a link.
 * Can only be used for shortest path routing.
 */
infix fun TemporaryLink.withCost(cost: Int): TemporaryLink {
    this.extender = ShortestPathExtender(cost)
    return this
}

//endregion

//region Interdomain Routing

infix fun TopologyBuilder<BGPRoute>.peerplusLink(createLink: () -> TemporaryLink) {
    val link = createLink()
    this.link(link.tail, link.head, PeerplusExtender)
}

infix fun TopologyBuilder<BGPRoute>.peerstarLink(createLink: () -> TemporaryLink) {
    val link = createLink()
    this.link(link.tail, link.head, PeerstarExtender)
}

infix fun TopologyBuilder<BGPRoute>.customerLink(createLink: () -> TemporaryLink) {
    val link = createLink()
    this.link(link.tail, link.head, CustomerExtender)
}

infix fun TopologyBuilder<BGPRoute>.peerLink(createLink: () -> TemporaryLink) {
    val link = createLink()
    this.link(link.tail, link.head, PeerExtender)
}

infix fun TopologyBuilder<BGPRoute>.providerLink(createLink: () -> TemporaryLink) {
    val link = createLink()
    this.link(link.tail, link.head, ProviderExtender)
}

infix fun TopologyBuilder<BGPRoute>.siblingLink(createLink: () -> TemporaryLink) {
    val link = createLink()
    this.link(link.tail, link.head, SiblingExtender)
}

//endregion