package bgp.notifications

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * An SelectListener listens for SelectNotifications.
 */
interface SelectListener {

    /**
     * Invoked to notify the listener of a new learn notification.
     */
    fun notify(notification: SelectNotification)
}