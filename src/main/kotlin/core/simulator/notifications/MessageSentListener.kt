package core.simulator.notifications

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 */
interface  MessageSentListener: NotificationListener {

    /**
     * Invoked to notify the listener of a new message sent notification.
     */
    fun notify(notification: MessageSentNotification)

}