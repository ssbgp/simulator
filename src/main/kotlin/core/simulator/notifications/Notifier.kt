package core.simulator.notifications

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 */
object Notifier {

    //region Lists containing the registered listeners

    private val startListeners = ArrayList<StartListener>()

    //endregion

    //region Start notification

    /**
     * Registers a new start listener.

     * @param listener start listener to register.
     */
    fun addStartListener(listener: StartListener) {
        startListeners.add(listener)
    }

    /**
     * Unregisters a new start listener.

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
        startListeners.forEach { listener -> listener.notifyStart(notification) }
    }

    //endregion

}