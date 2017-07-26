package testing

import core.simulator.notifications.*

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 *
 * The NotificationCollector collects all notifications send by the notifier.
 */
open class NotificationCollector : StartListener, EndListener {

    val startNotifications = ArrayList<StartNotification>()
    val endNotifications = ArrayList<EndNotification>()

    open fun register(notifier: BasicNotifier) {
        notifier.addStartListener(this)
        notifier.addEndListener(this)
    }

    open fun unregister(notifier: BasicNotifier) {
        notifier.removeStartListener(this)
        notifier.removeEndListener(this)
    }

    final override fun notify(notification: StartNotification) {
        startNotifications.add(notification)
    }

    final override fun notify(notification: EndNotification) {
        endNotifications.add(notification)
    }

}

fun collectNotificationsFom(notifier: BasicNotifier, body: () -> Unit): NotificationCollector {

    val collector = NotificationCollector()
    collector.register(notifier)
    body()
    collector.unregister(notifier)
    return collector
}