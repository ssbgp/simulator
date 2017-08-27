package bgp2.notifications

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * An MessageReceivedListener listens for MessageReceivedNotifications.
 */
interface MessageReceivedListener {

    /**
     * Invoked to notify the listener of a new message received notification.
     */
    fun notify(notification: MessageReceivedNotification)
}