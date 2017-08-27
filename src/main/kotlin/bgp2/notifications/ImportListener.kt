package bgp2.notifications

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * An ImportListener listens for ImportNotifications.
 */
interface ImportListener {

    /**
     * Invoked to notify the listener of a new import notification.
     */
    fun notify(notification: ImportNotification)
}