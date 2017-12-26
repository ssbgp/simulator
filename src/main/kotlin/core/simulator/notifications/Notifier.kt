package core.simulator.notifications

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * The [Notifier] is responsible for having each notification reach each listener listening
 * for that type of notification. The [Notifier] supports only the notifications included on
 * this package. Listeners need to registered with the [Notifier] to receive notifications.
 *
 * It provides three methods for each type of notification:
 *
 *   - add:    Called by a listener receive a specific type of notifications
 *   - remove: Called by a listener to stop receiving a specific type of notifications
 *   - notify: Called by the simulator during the simulation to send a notification to listeners
 *
 */
object Notifier {

    //region Lists containing the registered listeners

    private val startListeners = mutableListOf<StartListener>()
    private val endListeners = mutableListOf<EndListener>()
    private val thresholdReachedListeners = mutableListOf<ThresholdReachedListener>()
    private val messageSentListeners = mutableListOf<MessageSentListener>()
    private val messageReceivedListeners = mutableListOf<MessageReceivedListener>()

    //endregion

    //region Start notification

    /**
     * Tell the notifier that [listener] wants to receive start notifications. Afterwards, the
     * [listener] will receive all start notifications.
     */
    fun addStartListener(listener: StartListener) {
        startListeners.add(listener)
    }

    /**
     * Tell the notifier that [listener] no longer wants to receive start notifications.
     * Afterwards, the [listener] will no longer receive start notifications.
     */
    fun removeStartListener(listener: StartListener) {
        startListeners.remove(listener)
    }

    /**
     * Sends [notification] to all listeners listening to start notifications.
     */
    fun notify(notification: StartNotification) {
        startListeners.forEach { it.onStart(notification) }
    }

    //endregion

    //region End notification

    /**
     * Tell the notifier that [listener] wants to receive end notifications. Afterwards, the
     * [listener] will receive all end notifications.
     */
    fun addEndListener(listener: EndListener) {
        endListeners.add(listener)
    }

    /**
     * Tell the notifier that [listener] no longer wants to receive end notifications.
     * Afterwards, the [listener] will no longer receive end notifications.
     */
    fun removeEndListener(listener: EndListener) {
        endListeners.remove(listener)
    }

    /**
     * Sends [notification] to all listeners listening to end notifications.
     */
    fun notify(notification: EndNotification) {
        endListeners.forEach { it.onEnd(notification) }
    }

    //endregion

    //region Threshold reached notification

    /**
     * Tell the notifier that [listener] wants to receive threshold reached notifications.
     * Afterwards, the [listener] will receive all threshold reached notifications.
     */
    fun addThresholdReachedListener(listener: ThresholdReachedListener) {
        thresholdReachedListeners.add(listener)
    }

    /**
     * Tell the notifier that [listener] no longer wants to receive threshold reached notifications.
     * Afterwards, the [listener] will no longer receive threshold reached notifications.
     */
    fun removeThresholdReachedListener(listener: ThresholdReachedListener) {
        thresholdReachedListeners.remove(listener)
    }

    /**
     * Sends [notification] to all listeners listening to threshold reached notifications.
     */
    fun notify(notification: ThresholdReachedNotification) {
        thresholdReachedListeners.forEach { it.onThresholdReached(notification) }
    }

    //endregion

    //region Message sent notification

    /**
     * Tell the notifier that [listener] wants to receive message sent notifications. Afterwards,
     * the [listener] will receive all message sent notifications.
     */
    fun addMessageSentListener(listener: MessageSentListener) {
        messageSentListeners.add(listener)
    }

    /**
     * Tell the notifier that [listener] no longer wants to receive message sent notifications.
     * Afterwards, the [listener] will no longer receive message sent notifications.
     */
    fun removeMessageSentListener(listener: MessageSentListener) {
        messageSentListeners.remove(listener)
    }

    /**
     * Sends [notification] to all listeners listening to message sent notifications.
     */
    fun notify(notification: MessageSentNotification) {
        messageSentListeners.forEach { it.onMessageSent(notification) }
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
    fun notify(notification: MessageReceivedNotification) {
        messageReceivedListeners.forEach { it.onMessageReceived(notification) }
    }

    //endregion

}