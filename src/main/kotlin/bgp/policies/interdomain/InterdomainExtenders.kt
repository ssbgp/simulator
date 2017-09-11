package bgp.policies.interdomain

import bgp.BGPRoute
import core.routing.Extender
import core.routing.Node

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 */

const val LOCAL_PREF_PEERPLUS: Int = 500000
const val LOCAL_PREF_PEERSTAR: Int = 400000
const val LOCAL_PREF_CUSTOMER: Int = 300000
const val LOCAL_PREF_PEER: Int = 200000
const val LOCAL_PREF_PROVIDER: Int = 100000

object CustomerExtender: Extender<BGPRoute> {

    override fun extend(route: BGPRoute, sender: Node<BGPRoute>): BGPRoute {

        return when {
            route.localPref <= LOCAL_PREF_PEER || route.localPref == LOCAL_PREF_PEERSTAR  -> BGPRoute.invalid()
            else -> customerRoute(asPath = route.asPath.append(sender))
        }
    }

}

object PeerExtender: Extender<BGPRoute> {

    override fun extend(route: BGPRoute, sender: Node<BGPRoute>): BGPRoute {

        return when {
            route.localPref <= LOCAL_PREF_PEER || route.localPref == LOCAL_PREF_PEERSTAR  -> BGPRoute.invalid()
            else -> peerRoute(asPath = route.asPath.append(sender))
        }
    }

}

object ProviderExtender: Extender<BGPRoute> {

    override fun extend(route: BGPRoute, sender: Node<BGPRoute>): BGPRoute {

        return when {
            !route.isValid() -> BGPRoute.invalid()
            else -> providerRoute(asPath = route.asPath.append(sender))
        }
    }

}

object PeerplusExtender: Extender<BGPRoute> {

    override fun extend(route: BGPRoute, sender: Node<BGPRoute>): BGPRoute {

        return when {
            route.localPref <= LOCAL_PREF_PEER || route.localPref == LOCAL_PREF_PEERSTAR  -> BGPRoute.invalid()
            else -> peerplusRoute(asPath = route.asPath.append(sender))
        }
    }

}

object PeerstarExtender: Extender<BGPRoute> {

    override fun extend(route: BGPRoute, sender: Node<BGPRoute>): BGPRoute {

        return when {
            route.localPref <= LOCAL_PREF_PEER || route.localPref == LOCAL_PREF_PEERSTAR  -> BGPRoute.invalid()
            else -> peerstarRoute(asPath = route.asPath.append(sender))
        }
    }

}

object SiblingExtender: Extender<BGPRoute> {

    override fun extend(route: BGPRoute, sender: Node<BGPRoute>): BGPRoute {

        return when {
            !route.isValid() -> BGPRoute.invalid()
            route === BGPRoute.self() -> customerRoute(siblingHops = 1, asPath = route.asPath.append(sender))
            else -> BGPRoute.with(localPref = route.localPref - 1,
                    asPath = route.asPath.append(sender))
        }
    }

}