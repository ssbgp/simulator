package utils

import core.simulator.notifications.*

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 *
 * The NotificationCollector collects all notifications send by the notifier.
 */
open class NotificationCollector: StartListener, EndListener, ThresholdReachedListener,
        MessageSentListener, MessageReceivedListener {

    val startNotifications = ArrayList<StartNotification>()
    val endNotifications = ArrayList<EndNotification>()
    val thresholdReachedNotifications = ArrayList<ThresholdReachedNotification>()
    val messageSentNotifications = ArrayList<MessageSentNotification>()
    val messageReceivedNotifications = ArrayList<MessageReceivedNotification>()

    open fun register() {
        BasicNotifier.addStartListener(this)
        BasicNotifier.addEndListener(this)
        BasicNotifier.addThresholdReachedListener(this)
        BasicNotifier.addMessageSentListener(this)
        BasicNotifier.addMessageReceivedListener(this)
    }

    open fun unregister() {
        BasicNotifier.removeStartListener(this)
        BasicNotifier.removeEndListener(this)
        BasicNotifier.removeThresholdReachedListener(this)
        BasicNotifier.removeMessageSentListener(this)
        BasicNotifier.removeMessageReceivedListener(this)
    }

    final override fun notify(notification: StartNotification) {
        startNotifications.add(notification)
    }

    final override fun notify(notification: EndNotification) {
        endNotifications.add(notification)
    }

    final override fun notify(notification: ThresholdReachedNotification) {
        thresholdReachedNotifications.add(notification)
    }

    final override fun notify(notification: MessageSentNotification) {
        messageSentNotifications.add(notification)
    }

    final override fun notify(notification: MessageReceivedNotification) {
        messageReceivedNotifications.add(notification)
    }

}

fun collectBasicNotifications(body: () -> Unit): NotificationCollector {

    val collector = NotificationCollector()
    collector.register()
    body()
    collector.unregister()
    return collector
}