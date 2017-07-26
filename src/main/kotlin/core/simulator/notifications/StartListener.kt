package core.simulator.notifications

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * An StartListener listens for StartNotifications.
 */
interface StartListener : NotificationListener {

    /**
     * Invoked to notify the listener of a new start notification.
     */
    fun notify(notification: StartNotification)

}