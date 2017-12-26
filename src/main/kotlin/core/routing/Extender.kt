package core.routing

/**
 * An [Extender] is a transformation function for routes sent across a link.
 *
 * The Extender interface defines a single method [extend]. Subclasses should implement this
 * method to specify how routes are transformed by this extender.
 *
 * Extenders are associated with neighbors. They describe how a route selected at one node is
 * learned at the neighboring node. Ultimately, the extender associated with given neighbor
 * models the export policies of the local node and the import policies of the neighboring node.
 *
 * @see [bgp.policies] for examples on extender implementations.
 *
 * Created on 19-07-2017
 *
 * @author David Fialho
 */
interface Extender<R : Route> {

    /**
     * Takes a [route] and returns the extended route obtained from [route], according to the
     * function implemented by this extender. The output route may depend on the [sender], the
     * node which sent [route].
     */
    fun extend(route: R, sender: Node<R>): R

}