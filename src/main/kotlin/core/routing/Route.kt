package core.routing

/**
 * Created on 19-07-2017
 *
 * @author David Fialho
 *
 * A Route associates a set attributes with a destination. In a routing protocol, nodes exchange
 * routes between each other to provide connectivity to each other. The ultimate goal of a
 * routing protocol is to have each node select one or more routes to each that destination.
 *
 * Different routing protocols include different attributes in the exchanged routes. Thus, each
 * protocol implements their own route. This is the base interface for all route implementations.
 * Implementations of the Route interface should be always immutable!!
 *
 * Routes may be invalid. An invalid route indicates that there is no electable route to the
 * destination. A node selecting an invalid route, indicates the node did not had any valid
 * candidate route to reach the destination. To check whether or not a route is valid, use the
 * [isValid] method.
 */
interface Route {

    /**
     * Returns true if this route is valid or false if otherwise.
     */
    fun isValid(): Boolean

}