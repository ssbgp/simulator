package testing

import bgp.notifications.*

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
class BGPNotificationCollector : NotificationCollector(),
        MessageReceivedListener, ImportListener, LearnListener, SelectListener, ExportListener, MessageSentListener {

    //region Lists containing all notifications

    val messageReceivedNotifications = ArrayList<MessageReceivedNotification>()
    val importNotifications = ArrayList<ImportNotification>()
    val learnNotifications = ArrayList<LearnNotification>()
    val selectNotifications = ArrayList<SelectNotification>()
    val exportNotifications = ArrayList<ExportNotification>()
    val messageSentNotifications = ArrayList<MessageSentNotification>()

    //endregion

    //region Register/Unregister methods

    override fun register() {
        super.register()
        BGPNotifier.addMessageReceivedListener(this)
        BGPNotifier.addImportListener(this)
        BGPNotifier.addLearnListener(this)
        BGPNotifier.addSelectListener(this)
        BGPNotifier.addExportListener(this)
        BGPNotifier.addMessageSentListener(this)

    }

    override fun unregister() {
        super.unregister()
        BGPNotifier.removeMessageReceivedListener(this)
        BGPNotifier.removeImportListener(this)
        BGPNotifier.removeLearnListener(this)
        BGPNotifier.removeSelectListener(this)
        BGPNotifier.removeExportListener(this)
        BGPNotifier.removeMessageSentListener(this)
    }

    //endregion

    //region Notify methods

    override fun notify(notification: MessageReceivedNotification) {
        messageReceivedNotifications.add(notification)
    }

    override fun notify(notification: ImportNotification) {
        importNotifications.add(notification)
    }

    override fun notify(notification: LearnNotification) {
        learnNotifications.add(notification)
    }

    override fun notify(notification: SelectNotification) {
        selectNotifications.add(notification)
    }

    override fun notify(notification: ExportNotification) {
        exportNotifications.add(notification)
    }

    override fun notify(notification: MessageSentNotification) {
        messageSentNotifications.add(notification)
    }

    //endregion

}

fun collectBGPNotifications(body: () -> Unit): BGPNotificationCollector {

    val collector = BGPNotificationCollector()
    collector.register()
    body()
    collector.unregister()
    return collector
}