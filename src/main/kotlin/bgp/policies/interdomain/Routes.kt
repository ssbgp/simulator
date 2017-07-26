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
fun peerplusRoute(asPath: Path<BGPNode> = emptyPath()) = BGPRoute.with(localPref = LOCAL_PREF_PEERPLUS, asPath = asPath)

/**
 * Returns a customer route.
 */
fun customerRoute(asPath: Path<BGPNode> = emptyPath()) = BGPRoute.with(localPref = LOCAL_PREF_CUSTOMER, asPath = asPath)

/**
 * Returns a peer route.
 */
fun peerRoute(asPath: Path<BGPNode> = emptyPath()) = BGPRoute.with(localPref = LOCAL_PREF_PEER, asPath = asPath)

/**
 * Returns a provider route.
 */
fun providerRoute(asPath: Path<BGPNode> = emptyPath()) = BGPRoute.with(localPref = LOCAL_PREF_PROVIDER, asPath = asPath)
