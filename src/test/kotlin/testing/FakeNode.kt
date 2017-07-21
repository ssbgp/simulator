package testing

import core.routing.Node
import core.routing.NodeID

/**
 * Created on 21-07-2017
 *
 * @author David Fialho
 *
 * Fake implementation of a node to be used in tests that require any node instance.
 */
class FakeNode(id: NodeID) : Node(id)

/**
 * Returns a node instance associated with the given ID
 */
fun node(id: NodeID): Node {
    return FakeNode(id)
}