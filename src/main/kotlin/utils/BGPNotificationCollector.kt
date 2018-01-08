package utils

import bgp.notifications.*
import core.simulator.notifications.Notification

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
class BGPNotificationCollector(val withOutput: Boolean): NotificationCollector(),
        LearnListener, DetectListener, SelectListener, ExportListener {

    //region Lists containing all notifications

    val learnNotifications = ArrayList<LearnNotification>()
    val detectNotifications = ArrayList<DetectNotification>()
    val selectNotifications = ArrayList<SelectNotification>()
    val exportNotifications = ArrayList<ExportNotification>()

    //endregion

    //region Register/Unregister methods

    override fun register() {
        super.register()
        BGPNotifier.addLearnListener(this)
        BGPNotifier.addDetectListener(this)
        BGPNotifier.addSelectListener(this)
        BGPNotifier.addExportListener(this)

    }

    override fun unregister() {
        super.unregister()
        BGPNotifier.removeLearnListener(this)
        BGPNotifier.removeDetectListener(this)
        BGPNotifier.removeSelectListener(this)
        BGPNotifier.removeExportListener(this)
    }

    //endregion

    //region Notify methods

    override fun onLearn(notification: LearnNotification) {
        learnNotifications.add(notification)
        print(notification)
    }

    override fun onDetect(notification: DetectNotification) {
        detectNotifications.add(notification)
        print(notification)
    }

    override fun onSelect(notification: SelectNotification) {
        selectNotifications.add(notification)
        print(notification)
    }

    override fun onExport(notification: ExportNotification) {
        exportNotifications.add(notification)
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