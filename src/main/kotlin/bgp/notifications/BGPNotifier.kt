package bgp.notifications

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
object BGPNotifier{

    //region Lists containing the registered listeners

    private val learnListeners = mutableListOf<LearnListener>()
    private val detectListeners = mutableListOf<DetectListener>()
    private val selectListeners = mutableListOf<SelectListener>()
    private val exportListeners = mutableListOf<ExportListener>()

    //endregion

    //region Learn notification

    /**
     * Tell the notifier that [listener] wants to receive learn notifications. Afterwards, the
     * [listener] will receive all learn notifications.
     */
    fun addLearnListener(listener: LearnListener) {
        learnListeners.add(listener)
    }

    /**
     * Tell the notifier that [listener] no longer wants to receive learn notifications.
     * Afterwards, the [listener] will no longer receive learn notifications.
     */
    fun removeLearnListener(listener: LearnListener) {
        learnListeners.remove(listener)
    }

    /**
     * Sends [notification] to all listeners listening to learn notifications.
     */
    fun notify(notification: LearnNotification) {
        learnListeners.forEach { it.onLearn(notification) }
    }

    //endregion

    //region Detect notification

    /**
     * Tell the notifier that [listener] wants to receive detect notifications. Afterwards, the
     * [listener] will receive all detect notifications.
     */
    fun addDetectListener(listener: DetectListener) {
        detectListeners.add(listener)
    }

    /**
     * Tell the notifier that [listener] no longer wants to receive detect notifications.
     * Afterwards, the [listener] will no longer receive detect notifications.
     */
    fun removeDetectListener(listener: DetectListener) {
        detectListeners.remove(listener)
    }

    /**
     * Sends [notification] to all listeners listening to detect notifications.
     */
    fun notify(notification: DetectNotification) {
        detectListeners.forEach { it.onDetect(notification) }
    }

    //endregion

    //region Select notification

    /**
     * Tell the notifier that [listener] wants to receive select notifications. Afterwards, the
     * [listener] will receive all select notifications.
     */
    fun addSelectListener(listener: SelectListener) {
        selectListeners.add(listener)
    }

    /**
     * Tell the notifier that [listener] no longer wants to receive select notifications.
     * Afterwards, the [listener] will no longer receive select notifications.
     */
    fun removeSelectListener(listener: SelectListener) {
        selectListeners.remove(listener)
    }

    /**
     * Sends [notification] to all listeners listening to select notifications.
     */
    fun notify(notification: SelectNotification) {
        selectListeners.forEach { it.onSelect(notification) }
    }

    //endregion

    //region Export notification

    /**
     * Tell the notifier that [listener] wants to receive export notifications. Afterwards, the
     * [listener] will receive all export notifications.
     */
    fun addExportListener(listener: ExportListener) {
        exportListeners.add(listener)
    }

    /**
     * Tell the notifier that [listener] no longer wants to receive export notifications.
     * Afterwards, the [listener] will no longer receive export notifications.
     */
    fun removeExportListener(listener: ExportListener) {
        exportListeners.remove(listener)
    }

    /**
     * Sends [notification] to all listeners listening to export notifications.
     */
    fun notify(notification: ExportNotification) {
        exportListeners.forEach { it.onExport(notification) }
    }

    //endregion

}