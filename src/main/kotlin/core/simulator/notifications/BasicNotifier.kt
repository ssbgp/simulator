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

}