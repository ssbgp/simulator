package core.routing

/**
 * Created on 19-07-2017
 *
 * @author David Fialho
 *
 * Routes are pieces of routing information that associate a set of attributes with a destination. Usually, nodes
 * exchange routes with their neighbors in other to provide connectivity to one another.
 *
 * Routes may be invalid. An invalid route indicates that there is no electable route to the destination via some
 * neighbor, which means that an invalid route should NEVER be elected by a node. Routes can be checked for validity
 * through the isInvalid() method included in the Route interface.
 */
interface Route {

    /**
     * Returns true if a route is valid or false if otherwise.
     */
    fun isValid(): Boolean

}