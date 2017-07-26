package bgp.notifications

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * An LearnListener listens for LearnNotifications.
 */
interface LearnListener {

    /**
     * Invoked to notify the listener of a new learn notification.
     */
    fun notify(notification: LearnNotification)
}