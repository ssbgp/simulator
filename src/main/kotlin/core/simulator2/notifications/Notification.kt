package core.simulator2.notifications

import core.simulator.Time
import core.simulator.currentTime

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * @property time the time at which the notification was sent.
 */
abstract class Notification(val time: Time = currentTime())