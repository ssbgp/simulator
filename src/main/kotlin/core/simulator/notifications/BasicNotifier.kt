package core.simulator.notifications

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 */
object BasicNotifier : Notifier {

    //region Lists containing the registered listeners

    private val startListeners = mutableListOf<StartListener>()
    private val endListeners = mutableListOf<EndListener>()
    private val thresholdReachedListeners = mutableListOf<ThresholdReachedListener>()
    private val messageSentListeners = mutableListOf<MessageSentListener>()
    private val messageReceivedListeners = mutableListOf<MessageReceivedListener>()

    //endregion

    //region Start notification

    /**
     * Registers a new start listener.
     *
     * @param listener start listener to register.
     */
    fun addStartListener(listener: StartListener) {
        startListeners.add(listener)
    }

    /**
     * Unregisters a new start listener.
     *
     * @param listener start listener to unregister.
     */
    fun removeStartListener(listener: StartListener) {
        startListeners.remove(listener)
    }

    /**
     * Sends a start notification to each start listener.
     *
     * @param notification the start notification to send to each registered listener.
     */
    fun notifyStart(notification: StartNotification) {
        startListeners.forEach { it.notify(notification) }
    }

    //endregion

    //region End notification

    /**
     * Registers a new end listener.
     *
     * @param listener end listener to register.
     */
    fun addEndListener(listener: EndListener) {
        endListeners.add(listener)
    }

    /**
     * Unregisters a new end listener.
     *
     * @param listener end listener to unregister.
     */
    fun removeEndListener(listener: EndListener) {
        endListeners.remove(listener)
    }

    /**
     * Sends a end notification to each end listener.
     *
     * @param notification the end notification to send to each registered listener.
     */
    fun notifyEnd(notification: EndNotification) {
        endListeners.forEach { it.notify(notification) }
    }

    //endregion

    //region Threshold reached notification

    /**
     * Registers a new threshold reached listener.
     *
     * @param listener threshold reached listener to register.
     */
    fun addThresholdReachedListener(listener: ThresholdReachedListener) {
        thresholdReachedListeners.add(listener)
    }

    /**
     * Unregisters a new threshold reached listener.
     *
     * @param listener threshold reached listener to unregister.
     */
    fun removeThresholdReachedListener(listener: ThresholdReachedListener) {
        thresholdReachedListeners.remove(listener)
    }

    /**
     * Sends a threshold reached notification to each threshold reached listener.
     *
     * @param notification the threshold reached notification to send to each registered listener.
     */
    fun notifyThresholdReached(notification: ThresholdReachedNotification) {
        thresholdReachedListeners.forEach { it.notify(notification) }
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

}