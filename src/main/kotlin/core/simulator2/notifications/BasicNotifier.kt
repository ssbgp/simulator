package core.simulator2.notifications

import core.routing2.Route

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 */
object BasicNotifier : Notifier {

    //region Lists containing the registered listeners

    private val startListeners = mutableListOf<StartListener>()
    private val endListeners = mutableListOf<EndListener>()

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

}