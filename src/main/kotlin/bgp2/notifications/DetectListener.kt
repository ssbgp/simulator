package bgp2.notifications

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * An DetectListener listens for DetectNotifications.
 */
interface DetectListener {

    /**
     * Invoked to notify the listener of a new detect notification.
     */
    fun notify(notification: DetectNotification)
}