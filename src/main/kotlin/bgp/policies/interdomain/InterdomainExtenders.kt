package bgp.policies.interdomain

import bgp.BGPExtender
import bgp.BGPNode
import bgp.BGPRoute

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 */

const val LOCAL_PREF_PEERPLUS: Int = 400000
const val LOCAL_PREF_CUSTOMER: Int = 300000
const val LOCAL_PREF_PEER: Int = 200000
const val LOCAL_PREF_PROVIDER: Int = 100000

object CustomerExtender : BGPExtender {

    override fun extend(route: BGPRoute, sender: BGPNode): BGPRoute {

        return when {
            route.localPref <= LOCAL_PREF_PEER -> BGPRoute.invalid()
            else                               -> customerRoute(asPath = route.asPath.append(sender))
        }
    }

}

object PeerExtender : BGPExtender {

    override fun extend(route: BGPRoute, sender: BGPNode): BGPRoute {

        return when {
            route.localPref <= LOCAL_PREF_PEER -> BGPRoute.invalid()
            else                               -> peerRoute(asPath = route.asPath.append(sender))
        }
    }

}

object ProviderExtender : BGPExtender {

    override fun extend(route: BGPRoute, sender: BGPNode): BGPRoute {

        return when {
            !route.isValid() -> BGPRoute.invalid()
            else             -> providerRoute(asPath = route.asPath.append(sender))
        }
    }

}

object PeerplusExtender : BGPExtender {

    override fun extend(route: BGPRoute, sender: BGPNode): BGPRoute {

        return when {
            route.localPref <= LOCAL_PREF_PEER -> BGPRoute.invalid()
            else                               -> peerplusRoute(asPath = route.asPath.append(sender))
        }
    }

}

object SiblingExtender : BGPExtender {

    override fun extend(route: BGPRoute, sender: BGPNode): BGPRoute {

        return when {
            !route.isValid()          -> BGPRoute.invalid()
            route === BGPRoute.self() -> customerRoute(siblingHops = 1, asPath = route.asPath.append(sender))
            else                      -> BGPRoute.with(localPref = route.localPref + 1,
                                                       asPath = route.asPath.append(sender))
        }
    }

}