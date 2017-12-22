package core.simulator

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 *
 *
 *
 * A time starts in expired mode
 * The timer performs the specified action action whe it expires.
 *
 * @property isRunning flag indicating whether or not the timer is running
 */
interface Timer {

    val isRunning: Boolean

    /**
     * Cancels the timer if the timer as not expired yet.
     */
    fun cancel()

    /**
     * Should be called when the timer expires.
     */
    fun onExpired()

    companion object Factory {

        /**
         * Returns an enabled timer with the specified duration and action.
         */
        fun enabled(duration: Time, action: () -> Unit): Timer = EnabledTimer(duration, action)

        /**
         * Returns a disabled timer.
         */
        fun disabled(): Timer = DisabledTimer

    }

    /**
     * Timer implementation that represented an enabled timer. That is, it is a timer that actually works. Sew the
     * DisabledTimer below to understand what it means to say a timer is enabled/disabled.
     */
    private class EnabledTimer(duration: Time, private val action: () -> Unit) : Timer {

        override var isRunning: Boolean = true
            private set

        private var isCanceled = false

        init {
            Engine.scheduler.scheduleFromNow(TimerExpiredEvent(this), duration)
        }

        /**
         * Cancels the timer. If called before the timer expired, then the action of the timer is
         * not executed when this timer does expire. After calling [cancel] the timer stops running.
         */
        override fun cancel() {
            isCanceled = true
            isRunning = false
        }

        /**
         * Called when the timer expires to perform [action]. The [action] is performed if the
         * timer was not canceled.
         */
        override fun onExpired() {
            if (!isCanceled) {
                isRunning = false
                action()
            }
        }

    }

    /**
     * A disabled timer is a timer that does not work. That is, calling start does not start any timer.
     * Providing this timer implementation to an object is the same thing as saying that the timer used by that
     * object is disabled.
     */
    private object DisabledTimer : Timer {

        // A disabled timer never runs
        override val isRunning: Boolean = false

        override fun cancel() = Unit

        override fun onExpired() = Unit

    }

}