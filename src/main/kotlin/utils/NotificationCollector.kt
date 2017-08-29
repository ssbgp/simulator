package utils

import core.simulator2.notifications.*

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 *
 * The NotificationCollector collects all notifications send by the notifier.
 */
open class NotificationCollector: StartListener, EndListener {

    val startNotifications = ArrayList<StartNotification>()
    val endNotifications = ArrayList<EndNotification>()

    open fun register() {
        BasicNotifier.addStartListener(this)
        BasicNotifier.addEndListener(this)
    }

    open fun unregister() {
        BasicNotifier.removeStartListener(this)
        BasicNotifier.removeEndListener(this)
    }

    final override fun notify(notification: StartNotification) {
        startNotifications.add(notification)
    }

    final override fun notify(notification: EndNotification) {
        endNotifications.add(notification)
    }
}

fun collectBasicNotifications(body: () -> Unit): NotificationCollector {

    val collector = NotificationCollector()
    collector.register()
    body()
    collector.unregister()
    return collector
}