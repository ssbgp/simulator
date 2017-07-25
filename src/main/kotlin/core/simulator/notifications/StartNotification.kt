package core.simulator.notifications

import java.sql.Time

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * Notification sent before the simulation starts.
 */
data class StartNotification(override val time: Time, val seed: Long) : Notification