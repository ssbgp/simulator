package core.routing

typealias NodeID = Int

/**
 * Created on 19-07-17
 *
 * @author David Fialho
 *
 * A node is the fundamental component of a topology @see Topology. A node is some entity that is able to speak with
 * other nodes using a common protocol.
 *
 * This is the base class used to implement actual nodes that support some protocol. In its most simple form a node
 * is characterized by a unique ID. Subclasses of Node should provide the tools and information required for it to
 * communicate with other nodes of the same type using some common protocol.
 *
 * @property id The ID of the node. This ID uniquely identifies it inside a topology
 */
abstract class Node(val id: NodeID) {

    /**
     * Two nodes are considered equal if they have exactly the same ID.
     * Subclasses of node should not override this method.
     */
    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Node

        if (id != other.id) return false

        return true
    }

    /**
     * Follows the equals/hashCode contract.
     */
    final override fun hashCode(): Int {
        return id
    }

}