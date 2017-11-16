package bgp.policies.interdomain

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


// LOCAL-PREFs for each interdomain route
val peerplusLocalPreference: Int = 500000
val peerstarLocalPreference: Int = 400000
val customerLocalPreference: Int = 300000
val peerLocalPreference: Int = 200000
val providerLocalPreference: Int = 100000


/**
 * Returns a peer+ route.
 */
fun peerplusRoute(siblingHops: Int = 0, asPath: Path = emptyPath())
        = BGPRoute.with(localPref = peerplusLocalPreference - siblingHops, asPath = asPath)

/**
 * Returns a peer* route.
 */
fun peerstarRoute(siblingHops: Int = 0, asPath: Path = emptyPath())
        = BGPRoute.with(localPref = peerstarLocalPreference - siblingHops, asPath = asPath)

/**
 * Returns a customer route.
 */
fun customerRoute(siblingHops: Int = 0, asPath: Path = emptyPath())
        = BGPRoute.with(localPref = customerLocalPreference - siblingHops, asPath = asPath)

/**
 * Returns a peer route.
 */
fun peerRoute(siblingHops: Int = 0, asPath: Path = emptyPath())
        = BGPRoute.with(localPref = peerLocalPreference - siblingHops, asPath = asPath)

/**
 * Returns a provider route.
 */
fun providerRoute(siblingHops: Int = 0, asPath: Path = emptyPath())
        = BGPRoute.with(localPref = providerLocalPreference - siblingHops, asPath = asPath)
