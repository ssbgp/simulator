package testing

import bgp.BGPRoute
import core.routing.Topology
import core.simulator.Advertisement
import core.simulator.Advertiser
import core.simulator.Engine
import core.simulator.Time

/**
 * Created on 09-11-2017
 *
 * @author David Fialho
 */

fun simulate(topology: Topology<BGPRoute>, advertiser: Advertiser<BGPRoute>, threshold: Time = Int.MAX_VALUE): Boolean {
    return Engine.simulate(topology, Advertisement(advertiser, BGPRoute.self()), threshold)
}