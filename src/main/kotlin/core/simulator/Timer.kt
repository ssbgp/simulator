package core.simulator

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 *
 * A time starts in expired mode
 * The timer performs the specified action action whe it expires.
 */
sealed class Timer {

    // Flag indicating if the time has expired or not
    abstract var expired: Boolean
        protected set

    /**
     * Starts the timer if the timer has not expired. The timer is set to 'not expired' after calling start().
     *
     * @throws IllegalStateException if the timer has not expired yet
     */
    @Throws(IllegalStateException::class)
    abstract fun start()

    /**
     * Should be called when the timer expires.
     */
    abstract fun onExpired()

    companion object Factories {

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
    private class EnabledTimer(val duration: Time, private val action: () -> Unit) : Timer() {

        // At first the timer is set as expired to indicate that is available to be started
        override var expired = true

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
         * Should be called when the timer expires. It calls the action and sets the timer as 'expired'.
         */
        override fun onExpired() {
            expired = true
            action()
        }

    }

    /**
     * A disabled timer is a timer that does not work. That is, calling start does not start any timer.
     * Providing this timer implementation to an object is the same thing as saying that the timer used by that
     * object is disabled.
     */
    private object DisabledTimer : Timer() {

        // A disabled timer is never expired
        override var expired: Boolean = false

        /**
         * Does not start anything.
         */
        override fun start() = Unit

        /**
         * Does nothing, because this should never be called.
         */
        override fun onExpired() = Unit

    }

}