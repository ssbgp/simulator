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
            !route.isValid() -> BGPRoute.invalid()
            else             -> customerRoute(route.asPath.append(sender))
        }
    }

}