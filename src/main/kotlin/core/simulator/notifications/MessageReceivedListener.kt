package core.simulator.notifications

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 */
interface MessageReceivedListener: NotificationListener {

    /**
     * Invoked to notify the listener of a new message received notification.
     */
    fun notify(notification: MessageReceivedNotification)

}