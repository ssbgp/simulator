package bgp.notifications

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * An MessageSentListener listens for MessageSentNotifications.
 */
interface MessageSentListener {

    /**
     * Invoked to notify the listener of a new message sent notification.
     */
    fun notify(notification: MessageSentNotification)
}