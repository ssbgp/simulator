package testing

import bgp.*
import bgp.policies.interdomain.*
import bgp.policies.shortestpath.ShortestPathExtender
import core.simulator.DelayGenerator
import core.simulator.ZeroDelayGenerator

/**
 * Created on 24-07-2017.
 *
 * @author David Fialho
 */

/**
 *
 */
object DummyBGPExtender : BGPExtender { override fun extend(route: BGPRoute, sender: BGPNode) = route }

/**
 *
 */
data class Link(val tail: Int, val head: Int, var extender: BGPExtender = DummyBGPExtender,
                var delayGenerator: DelayGenerator = ZeroDelayGenerator)

/**
 *
 */
fun bgpTopology(body: BGPTopologyBuilder.() -> Unit): BGPTopology {

    val builder = BGPTopologyBuilder()
    body(builder)
    return builder.build()
}

/**
 *
 */
fun ssbgpTopology(body: BGPTopologyBuilder.() -> Unit): BGPTopology {

    val builder = BGPTopologyBuilder()
    body(builder)
    return builder.build()
}

infix fun Int.to(head: Int) = Link(tail = this, head = head)

infix fun Link.using(extender: BGPExtender): Link {
    this.extender = extender
    return this
}

infix fun Link.delaysFrom(delayGenerator: DelayGenerator): Link {
    this.delayGenerator = delayGenerator
    return this
}

infix fun Int.using(protocol: BaseBGPProtocol): Pair<Int, BaseBGPProtocol> {
    return Pair(this, protocol)
}

fun BGPTopologyBuilder.node(nodePair: () -> Pair<Int, BaseBGPProtocol>) {

    val pair = nodePair()
    this.addNode(id = pair.first, protocol = pair.second)
}

fun BGPTopologyBuilder.link(createLink: () -> Link) {

    val link = createLink()
    this.addLink(link.tail, link.head, link.extender)
}

//region Extension functions specific to shortest-path routing

infix fun Link.withCost(cost: Int): Link {
    this.extender = ShortestPathExtender(cost)
    return this
}

infix fun BGPTopologyBuilder.peerplusLink(createLink: () -> Link) {
    val link = createLink()
    this.addLink(link.tail, link.head, PeerplusExtender)
}

infix fun BGPTopologyBuilder.customerLink(createLink: () -> Link) {
    val link = createLink()
    this.addLink(link.tail, link.head, CustomerExtender)
}

infix fun BGPTopologyBuilder.peerLink(createLink: () -> Link) {
    val link = createLink()
    this.addLink(link.tail, link.head, PeerExtender)
}

infix fun BGPTopologyBuilder.providerLink(createLink: () -> Link) {
    val link = createLink()
    this.addLink(link.tail, link.head, ProviderExtender)
}

infix fun BGPTopologyBuilder.siblingLink(createLink: () -> Link) {
    val link = createLink()
    this.addLink(link.tail, link.head, SiblingExtender)
}

//endregion
