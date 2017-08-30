package core.simulator.notifications

import core.routing.Topology

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * Notification sent before the simulation starts.
 *
 * @property seed     the initial seed used to generate the communication delays
 * @property topology the topology used fot the simulation that is about to start
 */
data class StartNotification(val seed: Long, val topology: Topology<*>) : Notification()