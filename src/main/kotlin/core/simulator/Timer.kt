package core.simulator

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 *
 * A time starts in expired mode
 * The timer performs the specified action action whe it expires.
 */
class Timer(val duration: Time, private val action: () -> Unit) {

    // Flags indicating if the time has expired or not
    var expired = true
        private set

    /**
     * Starts the timer if the timer has not expired. The timer is set to 'not expired' after calling start().
     *
     * @throws IllegalStateException if the timer has not expired yet
     */
    @Throws(IllegalStateException::class)
    fun start() {

        if (!expired) throw IllegalStateException("Can not start an expired timer")

        Scheduler.scheduleFromNow(TimerExpiredEvent(this), duration)
        expired = false
    }

    /**
     * Should be called when the timer expires. It calls the action and sets the timer as 'expired'.
     */
    fun onExpired() {
        expired = true
        action()
    }

}