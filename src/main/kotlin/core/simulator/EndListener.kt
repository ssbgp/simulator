package core.simulator

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * An EndListener listens for EndNotifications.
 */
interface EndListener {

    /**
     * Invoked to notify the listener of a new end notification.
     */
    fun notify(notification: EndNotification)
}