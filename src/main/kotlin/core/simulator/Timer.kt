package core.simulator

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 *
 * A time starts in expired mode
 * The timer performs the specified action action whe it expires.
 */
interface Timer {

    // Flag indicating if the time has expired or not
    val expired: Boolean

    /**
     * Starts the timer if the timer has not expired. The timer is set to 'not expired' after calling start().
     *
     * @throws IllegalStateException if the timer has not expired yet
     */
    @Throws(IllegalStateException::class)
    fun start()

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
    private class EnabledTimer(val duration: Time, private val action: () -> Unit) : Timer {

        // At first the timer is set as expired to indicate that is available to be started
        override var expired = true
            private set

        // Flags used to indicate if a timer was canceled
        private var canceled = false

        /**
         * Starts the timer. Started timer will expire 'duration' units of time from now.
         */
        @Throws(IllegalStateException::class)
        override fun start() {

            if (!expired) throw IllegalStateException("Can not start an expired timer")

            Engine.scheduler.scheduleFromNow(TimerExpiredEvent(this), duration)
            expired = false
        }

        /**
         * Avoids the action of the time being performed when the timer expires. If the timer has already expired
         * then it does nothing.
         */
        override fun cancel() {
            if (!expired) canceled = true
        }

        /**
         * Should be called when the timer expires. It calls the action and sets the timer as 'expired'.
         */
        override fun onExpired() {

            expired = true
            if (!canceled) action()
            canceled = false
        }

    }

    /**
     * A disabled timer is a timer that does not work. That is, calling start does not start any timer.
     * Providing this timer implementation to an object is the same thing as saying that the timer used by that
     * object is disabled.
     */
    private object DisabledTimer : Timer {

        // A disabled timer is never expired
        override val expired: Boolean = true

        /**
         * Does not start anything.
         */
        override fun start() = Unit

        /**
         * Does nothing because a disabled timer itself does nothing.
         */
        override fun cancel() = Unit

        /**
         * Does nothing, because this should never be called.
         */
        override fun onExpired() = Unit

    }

}