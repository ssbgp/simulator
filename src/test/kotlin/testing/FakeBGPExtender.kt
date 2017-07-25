package testing

import bgp.BGPExtender
import bgp.BGPNode
import bgp.BGPRoute

/**
 * Created on 25-07-2017
 *
 * @author David Fialho
 *
 * Fake extender used for testing purposes.
 */
object FakeBGPExtender : BGPExtender {
    override fun extend(route: BGPRoute, sender: BGPNode): BGPRoute = BGPRoute.invalid()
}

/**
 * Returns an extender when it is needed one but it is not important which one.
 */
fun someExtender(): BGPExtender {
    return FakeBGPExtender
}