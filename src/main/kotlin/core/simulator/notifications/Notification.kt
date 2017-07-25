package core.simulator.notifications

import java.sql.Time

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 */
interface Notification {

    /**
     * Time at which the notification was generated.
     */
    val time: Time
}