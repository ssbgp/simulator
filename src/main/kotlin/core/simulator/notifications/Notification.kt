package core.simulator.notifications

import core.simulator.Time

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * @property time the time at which the notification was sent.
 */
interface Notification {

    /**
     * Time at which the notification was generated.
     */
    val time: Time
}