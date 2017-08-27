package bgp2.notifications

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * An ReEnableListener listens for ReEnableNotifications.
 */
interface ReEnableListener {

    /**
     * Invoked to notify the listener of a new re-enable notification.
     */
    fun notify(notification: ReEnableNotification)
}