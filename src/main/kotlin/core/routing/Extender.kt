package core.routing

/**
 * Created on 19-07-2017
 *
 * @author David Fialho
 *
 * An extender is a transformation function. It is always associated with a link between two neighboring nodes.
 * This function describes how each route elected by the head node of the link is transformed at the tail node.
 * It describes both the export bgp.policies of the head node and the import bgp.policies of the tail node.
 *
 * TODO @doc - improve the documentation for extender
 */
interface Extender<R: Route> {

    /**
     * Takes a route and returns a new route with the attributes defined according to the implementation of the
     * extender function.
     *
     * @param route  the route to be extended
     * @param sender the node that sends the route
     * @return the extended route
     */
    fun extend(route: R, sender: Node<R>): R

}