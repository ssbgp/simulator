package testing

import bgp.notifications.*
import core.simulator.notifications.Notification

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
class BGPNotificationCollector(val withOutput: Boolean) : NotificationCollector(),
        MessageReceivedListener, ImportListener, LearnListener, DetectListener, SelectListener, ExportListener,
        MessageSentListener, ReEnableListener {

    //region Lists containing all notifications

    val messageReceivedNotifications = ArrayList<MessageReceivedNotification>()
    val importNotifications = ArrayList<ImportNotification>()
    val learnNotifications = ArrayList<LearnNotification>()
    val detectNotifications = ArrayList<DetectNotification>()
    val selectNotifications = ArrayList<SelectNotification>()
    val exportNotifications = ArrayList<ExportNotification>()
    val messageSentNotifications = ArrayList<MessageSentNotification>()
    val reEnableNotifications = ArrayList<ReEnableNotification>()

    //endregion

    //region Register/Unregister methods

    override fun register() {
        super.register()
        BGPNotifier.addMessageReceivedListener(this)
        BGPNotifier.addImportListener(this)
        BGPNotifier.addLearnListener(this)
        BGPNotifier.addDetectListener(this)
        BGPNotifier.addSelectListener(this)
        BGPNotifier.addExportListener(this)
        BGPNotifier.addMessageSentListener(this)
        BGPNotifier.addReEnableListener(this)

    }

    override fun unregister() {
        super.unregister()
        BGPNotifier.removeMessageReceivedListener(this)
        BGPNotifier.removeImportListener(this)
        BGPNotifier.removeLearnListener(this)
        BGPNotifier.removeDetectListener(this)
        BGPNotifier.removeSelectListener(this)
        BGPNotifier.removeExportListener(this)
        BGPNotifier.removeMessageSentListener(this)
        BGPNotifier.removeReEnableListener(this)
    }

    //endregion

    //region Notify methods

    override fun notify(notification: MessageReceivedNotification) {
        messageReceivedNotifications.add(notification)
        print(notification)
    }

    override fun notify(notification: ImportNotification) {
        importNotifications.add(notification)
        print(notification)
    }

    override fun notify(notification: LearnNotification) {
        learnNotifications.add(notification)
        print(notification)
    }

    override fun notify(notification: DetectNotification) {
        detectNotifications.add(notification)
        print(notification)
    }

    override fun notify(notification: SelectNotification) {
        selectNotifications.add(notification)
        print(notification)
    }

    override fun notify(notification: ExportNotification) {
        exportNotifications.add(notification)
        print(notification)
    }

    override fun notify(notification: MessageSentNotification) {
        messageSentNotifications.add(notification)
        print(notification)
    }

    override fun notify(notification: ReEnableNotification) {
        reEnableNotifications.add(notification)
        print(notification)
    }

    private fun print(notification: Notification) {
        if (withOutput) {
            println("time=${notification.time}: $notification")
        }
    }

    //endregion

}

fun collectBGPNotifications(withOutput: Boolean = false, body: () -> Unit): BGPNotificationCollector {

    val collector = BGPNotificationCollector(withOutput)
    collector.register()
    body()
    collector.unregister()
    return collector
}