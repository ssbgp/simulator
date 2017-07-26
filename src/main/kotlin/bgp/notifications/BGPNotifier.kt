package bgp.notifications

import core.simulator.notifications.BasicNotifier

/**
 * Created on 26-07-2017
 *
 * @author David Fialho
 */
open class BGPNotifier : BasicNotifier() {

    //region Lists containing the registered listeners

    private val messageReceivedListeners = mutableListOf<MessageReceivedListener>()
    private val importListeners = mutableListOf<ImportListener>()
    private val learnListeners = mutableListOf<LearnListener>()
    private val selectListeners = mutableListOf<SelectListener>()
    private val exportListeners = mutableListOf<ExportListener>()
    private val messageSentListeners = mutableListOf<MessageSentListener>()

    //endregion

    //region Message received notification

    /**
     * Registers a new message received listener.
     *
     * @param listener message received listener to register.
     */
    fun addMessageReceivedListener(listener: MessageReceivedListener) {
        messageReceivedListeners.add(listener)
    }

    /**
     * Unregisters a new message received listener.
     *
     * @param listener message received listener to unregister.
     */
    fun removeMessageReceivedListener(listener: MessageReceivedListener) {
        messageReceivedListeners.remove(listener)
    }

    /**
     * Sends a message received notification to each message received listener.
     *
     * @param notification the message received notification to send to each registered listener.
     */
    fun notifyMessageReceived(notification: MessageReceivedNotification) {
        messageReceivedListeners.forEach { it.notify(notification) }
    }

    //endregion

    //region Import notification

    /**
     * Registers a new import listener.
     *
     * @param listener import listener to register.
     */
    fun addImportListener(listener: ImportListener) {
        importListeners.add(listener)
    }

    /**
     * Unregisters a new import listener.
     *
     * @param listener import listener to unregister.
     */
    fun removeImportListener(listener: ImportListener) {
        importListeners.remove(listener)
    }

    /**
     * Sends a import notification to each import listener.
     *
     * @param notification the import notification to send to each registered listener.
     */
    fun notifyImport(notification: ImportNotification) {
        importListeners.forEach { it.notify(notification) }
    }

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

    //region Message sent notification

    /**
     * Registers a new message sent listener.
     *
     * @param listener message sent listener to register.
     */
    fun addMessageSentListener(listener: MessageSentListener) {
        messageSentListeners.add(listener)
    }

    /**
     * Unregisters a new message sent listener.
     *
     * @param listener message sent listener to unregister.
     */
    fun removeMessageSentListener(listener: MessageSentListener) {
        messageSentListeners.remove(listener)
    }

    /**
     * Sends a message sent notification to each message sent listener.
     *
     * @param notification the message sent notification to send to each registered listener.
     */
    fun notifyMessageSent(notification: MessageSentNotification) {
        messageSentListeners.forEach { it.notify(notification) }
    }

    //endregion

}