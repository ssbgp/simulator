package core.simulator

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 *
 * A [Timer] is used to schedule an action to be performed after some period of time.
 * Timer objects are one-time use objects. That is, a timer starts immediately after it is
 * created and it can only be started once. After expiring, that object is completely useless.
 *
 * There are two [Timer] implementations: EnabledTimer and DisabledTimer. The former is the
 * actual implementation of timer. The latter is just a dummy implementation to represent a timer
 * that is disabled, thus, never runs.
 *
 * @property isRunning flag indicating whether or not the timer is running
 */
sealed class Timer {

    abstract val isRunning: Boolean

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
     * Cancels the timer if the timer as not expired yet.
     */
    abstract fun cancel()

    /**
     * Should be called when the timer expires.
     */
    abstract fun onExpired()

    private class EnabledTimer(duration: Time, private val action: () -> Unit) : Timer() {

        override var isRunning: Boolean = true
            private set

        private var isCanceled = false

        init {
            Simulator.scheduler.scheduleFromNow(TimerExpiredEvent(this), duration)
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

    private object DisabledTimer : Timer() {

        // A disabled timer never runs
        override val isRunning: Boolean = false

        override fun cancel() = Unit

        override fun onExpired() = Unit

    }

}