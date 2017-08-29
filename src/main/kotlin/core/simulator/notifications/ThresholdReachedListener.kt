package core.simulator.notifications

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * An core.simulator.notifications.ThresholdReachedListener listens for ThresholdReachedNotifications.
 */
interface ThresholdReachedListener {

    /**
     * Invoked to notify the listener of a new threshold reached notification.
     */
    fun notify(notification: ThresholdReachedNotification)
}