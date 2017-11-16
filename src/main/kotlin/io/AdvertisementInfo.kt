package io

import core.routing.NodeID
import core.routing.Route
import core.simulator.Time

/**
 * Created on 14-11-2017
 *
 * @author David Fialho
 */
data class AdvertisementInfo<out R: Route>(val advertiserID: NodeID, val defaultRoute: R, val time: Time)