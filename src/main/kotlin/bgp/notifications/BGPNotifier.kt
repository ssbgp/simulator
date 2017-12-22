package bgp.notifications

import core.simulator.notifications.Notifier

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
object BGPNotifier: Notifier {

    //region Lists containing the registered listeners

    private val learnListeners = mutableListOf<LearnListener>()
    private val detectListeners = mutableListOf<DetectListener>()
    private val selectListeners = mutableListOf<SelectListener>()
    private val exportListeners = mutableListOf<ExportListener>()

    //endregion

    //region Learn notification

    /**
     * Registers a new learn listener.
     *
     * @param listener learn listener to register.
     */
    fun addLearnListener(listener: LearnListener) {
        learnListeners.add(listener)
    }

    /**
     * Unregisters a new learn listener.
     *
     * @param listener learn listener to unregister.
     */
    fun removeLearnListener(listener: LearnListener) {
        learnListeners.remove(listener)
    }

    /**
     * Sends a learn notification to each learn listener.
     *
     * @param notification the learn notification to send to each registered listener.
     */
    fun notifyLearn(notification: LearnNotification) {
        learnListeners.forEach { it.notify(notification) }
    }

    //endregion

    //region Detect notification

    /**
     * Registers a new detect listener.
     *
     * @param listener detect listener to register.
     */
    fun addDetectListener(listener: DetectListener) {
        detectListeners.add(listener)
    }

    /**
     * Unregisters a new detect listener.
     *
     * @param listener detect listener to unregister.
     */
    fun removeDetectListener(listener: DetectListener) {
        detectListeners.remove(listener)
    }

    /**
     * Sends a detect notification to each detect listener.
     *
     * @param notification the detect notification to send to each registered listener.
     */
    fun notifyDetect(notification: DetectNotification) {
        detectListeners.forEach { it.notify(notification) }
    }

    //endregion

    //region Select notification

    /**
     * Registers a new select listener.
     *
     * @param listener select listener to register.
     */
    fun addSelectListener(listener: SelectListener) {
        selectListeners.add(listener)
    }

    /**
     * Unregisters a new select listener.
     *
     * @param listener select listener to unregister.
     */
    fun removeSelectListener(listener: SelectListener) {
        selectListeners.remove(listener)
    }

    /**
     * Sends a select notification to each select listener.
     *
     * @param notification the select notification to send to each registered listener.
     */
    fun notifySelect(notification: SelectNotification) {
        selectListeners.forEach { it.notify(notification) }
    }

    //endregion

    //region Export notification

    /**
     * Registers a new export listener.
     *
     * @param listener export listener to register.
     */
    fun addExportListener(listener: ExportListener) {
        exportListeners.add(listener)
    }

    /**
     * Unregisters a new export listener.
     *
     * @param listener export listener to unregister.
     */
    fun removeExportListener(listener: ExportListener) {
        exportListeners.remove(listener)
    }

    /**
     * Sends a export notification to each export listener.
     *
     * @param notification the export notification to send to each registered listener.
     */
    fun notifyExport(notification: ExportNotification) {
        exportListeners.forEach { it.notify(notification) }
    }

    //endregion

}