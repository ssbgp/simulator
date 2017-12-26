package core.simulator.notifications

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * Tag interface for notification listeners.
 *
 * During a simulation multiple notifications are issued. These notifications provide valuable
 * information about the state and progress of the routing protocol(s) being simulated. Thus,
 * listening these notifications may be useful to collect relevant data.
 *
 * A [NotificationListener] listens for a specific notification. To listen for multiple
 * notifications, the subclass should implement multiple types of listeners.
 */
interface NotificationListener

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * A [StartListener] listens for [StartNotification]s.
 */
interface StartListener : NotificationListener {

    /**
     * Invoked when a start notification is issued.
     */
    fun onStart(notification: StartNotification)
}

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * An [EndListener] listens for [EndNotification]s.
 */
interface EndListener {

    /**
     * Invoked when an end notification is issued.
     */
    fun onEnd(notification: EndNotification)
}

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * A [ThresholdReachedListener] listens for [ThresholdReachedNotification]s.
 */
interface ThresholdReachedListener {

    /**
     * Invoked when a threshold reached notification is issued.
     */
    fun onThresholdReached(notification: ThresholdReachedNotification)
}

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 *
 * A [MessageSentListener] listens for [MessageSentNotification]s.
 */
interface MessageSentListener : NotificationListener {

    /**
     * Invoked when a message sent notification is issued.
     */
    fun onMessageSent(notification: MessageSentNotification)
}

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 *
 * A [MessageReceivedListener] listens for [MessageReceivedNotification]s.
 */
interface MessageReceivedListener : NotificationListener {

    /**
     * Invoked when a message received notification is issued.
     */
    fun onMessageReceived(notification: MessageReceivedNotification)
}