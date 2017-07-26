package core.simulator.notifications

import core.simulator.Time

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * Notification sent before the simulation starts.
 *
 * @property seed the initial seed used to generate the communication delays
 */
data class StartNotification(val seed: Long) : Notification()