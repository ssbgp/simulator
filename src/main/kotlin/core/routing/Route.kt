package core.routing

/**
 * A Route associates a set attributes with a destination.
 *
 * In a routing protocol, nodes exchange routes between each other to provide connectivity to
 * each other. The ultimate goal of a routing protocol is to have each node select one or more
 * routes to each that destination.
 *
 * Routes exchanged in different routing protocols have different attributes. Therefore, each
 * protocol implementation must define their own route implementation. This is the base interface
 * for all route implementations.
 *
 * Routes may be invalid. An invalid route indicates that there is no electable route to the
 * destination. A node selecting an invalid route, indicates the node did not had any valid
 * candidate route to reach the destination. To check whether or not a route is valid, use the
 * [isValid] method.
 *
 * *Warning:* Implementations of the [Route] interface should always be immutable!!
 *
 * Created on 19-07-2017
 *
 * @author David Fialho
 */
interface Route {

    /**
     * Returns true if this route is valid or false if otherwise.
     */
    fun isValid(): Boolean

}