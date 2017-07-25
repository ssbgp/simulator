package core.routing

import bgp.BGPNode

/**
 * Created on 16-07-2017.
 *
 * @author David Fialho
 *
 * Topology is an high-level abstraction of a network composed of nodes and their interconnections.
 * This interface defines the interface that is common to all topologies.
 *
 * Notice the Topology interface does not define any methods to add/remove nodes or links. Topology implementations
 * should be immutable! That is, there should be no way to add or remove nodes from the topology and the same should
 * be true for links. A topology should be built using a topology builder @see TopologyBuilder.
 *
 * Each node in a topology is uniquely identified by its ID. Therefore, the topology interface provides methods to
 * access topology nodes using their IDs.
 *
 * This interface takes a generic type N that extends from Node. N is the type of nodes that the topology holds.
 *
 * @property size the number of nodes in the topology
 */
interface Topology<N: Node, R: Route> {

    /**
     * Returns the number of nodes in the topology.
     */
    val size: Int

    /**
     * Returns the number of links in the topology.
     */
    val linkCount: Int

    /**
     * Returns the node associated with the given ID.

     * @param id the ID of the node to get from the network.
     * @return the node associated with the given ID or null if the topology does not contain a node with such an ID.
     */
    operator fun get(id: Int): BGPNode?

    /**
     * Returns a collection with all nodes contained in the topology in no particular order.
     *
     * @return a collection with all nodes contained in the topology in no particular order.
     */
    fun getNodes(): Collection<N>

    /**
     * Returns a collection with all links contained in the topology in no particular order.
     *
     * @return a collection with all links contained in the topology in no particular order.
     */
    fun getLinks(): Collection<Link<N, R>>

}

/**
 * Represents a link in the topology.
 */
data class Link<N: Node, R: Route>(val tail: N, val head: N, val extender: Extender<N, R>)
