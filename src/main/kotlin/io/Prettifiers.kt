package io

import bgp.BGPRoute
import core.routing.Node
import core.routing.Path
import core.routing.Route

/**
 * Created on 16-11-2017
 *
 * @author David Fialho
 *
 * This files contains extension methods to prettify some classes that are output to the user.
 */

/**
 * Returns the node's ID as a string.
 */
fun <R: Route> Node<R>.pretty(): String = id.toString()

/**
 * Returns the IDs of the nodes in the path separated by a comma.
 */
fun Path.pretty(): String = joinToString(transform = {it.pretty()})

/**
 * Converts the local preference of a BGP route to the corresponding interdomain label and the AS path to a path.
 * These are put inside parenthesis and separated by a comma.
 */
fun BGPRoute.pretty(): String {

    if (this === BGPRoute.invalid() || this === BGPRoute.self()) {
        return toString()
    }

    return "(${localPref.toInterdomainLabel()}, ${asPath.pretty()})"
}