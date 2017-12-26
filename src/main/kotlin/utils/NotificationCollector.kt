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
        Notifier.addStartListener(this)
        Notifier.addEndListener(this)
        Notifier.addThresholdReachedListener(this)
        Notifier.addMessageSentListener(this)
        Notifier.addMessageReceivedListener(this)
    }

    open fun unregister() {
        Notifier.removeStartListener(this)
        Notifier.removeEndListener(this)
        Notifier.removeThresholdReachedListener(this)
        Notifier.removeMessageSentListener(this)
        Notifier.removeMessageReceivedListener(this)
    }

    final override fun onStart(notification: StartNotification) {
        startNotifications.add(notification)
    }

    final override fun onEnd(notification: EndNotification) {
        endNotifications.add(notification)
    }

    final override fun onThresholdReached(notification: ThresholdReachedNotification) {
        thresholdReachedNotifications.add(notification)
    }

    final override fun onMessageSent(notification: MessageSentNotification) {
        messageSentNotifications.add(notification)
    }

    final override fun onMessageReceived(notification: MessageReceivedNotification) {
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