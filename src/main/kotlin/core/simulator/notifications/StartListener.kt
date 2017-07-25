package core.simulator.notifications

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 */
interface StartListener : NotificationListener {

    /**
     * Invoked to notify the listener of a new start notification.
     */
    fun notifyStarted(notification: StartNotification)

}