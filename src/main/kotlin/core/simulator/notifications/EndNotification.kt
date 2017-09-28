package core.simulator.notifications

import core.routing.Topology


/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 */
data class EndNotification(val topology: Topology<*>) : Notification()