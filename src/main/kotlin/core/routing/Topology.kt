package core.routing

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
class Topology<R: Route>(private val idToNode: Map<NodeID, Node<R>>) {

    /**
     * Number of nodes in the topology.
     */
    val size: Int = idToNode.size

    /**
     * Number of links in the topology.
     */
    val linkCount: Int
        get() = idToNode.flatMap { it.value.inNeighbors }.count()

    /**
     * Collection containing all nodes in the topology in no particular order.
     */
    val nodes: Collection<Node<R>> = idToNode.values

    /**
     * Collection containing all links in the topology in no particular order.
     */
    val links: Collection<Link<R>>
        get() {

            val links = ArrayList<Link<R>>()

            for (node in nodes) {
                for ((neighbor, extender, _) in node.inNeighbors) {
                    links.add(Link(neighbor, node, extender))
                }
            }

            return links
        }


    /**
     * Returns the node associated with the given ID.

     * @param id the ID of the node to get from the network.
     * @return the node associated with the given ID or null if the topology does not contain a node with such an ID.
     */
    operator fun get(id: Int): Node<R>? = idToNode[id]

}
