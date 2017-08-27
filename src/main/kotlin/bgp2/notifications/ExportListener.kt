package bgp2.notifications

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * An ExportListener listens for ExportNotifications.
 */
interface ExportListener {

    /**
     * Invoked to notify the listener of a new export notification.
     */
    fun notify(notification: ExportNotification)
}