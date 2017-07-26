package bgp.policies.interdomain

import bgp.BGPNode
import bgp.BGPRoute
import core.routing.Path
import core.routing.emptyPath

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 *
 * This file contains methods to construct interdomain routes.
 */

/**
 * Returns a peer+ route.
 */
fun peerplusRoute(siblingHops: Int = 0, asPath: Path<BGPNode> = emptyPath())
        = BGPRoute.with(localPref = LOCAL_PREF_PEERPLUS - siblingHops, asPath = asPath)

/**
 * Returns a customer route.
 */
fun customerRoute(siblingHops: Int = 0, asPath: Path<BGPNode> = emptyPath())
        = BGPRoute.with(localPref = LOCAL_PREF_CUSTOMER - siblingHops, asPath = asPath)

/**
 * Returns a peer route.
 */
fun peerRoute(siblingHops: Int = 0, asPath: Path<BGPNode> = emptyPath())
        = BGPRoute.with(localPref = LOCAL_PREF_PEER - siblingHops, asPath = asPath)

/**
 * Returns a provider route.
 */
fun providerRoute(siblingHops: Int = 0, asPath: Path<BGPNode> = emptyPath())
        = BGPRoute.with(localPref = LOCAL_PREF_PROVIDER - siblingHops, asPath = asPath)
