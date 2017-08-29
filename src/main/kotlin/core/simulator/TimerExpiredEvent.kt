package core.simulator

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 */
class TimerExpiredEvent(private val timer: Timer) : Event {

    /**
     * Calls the onExpired() method of the timer that expired.
     */
    override fun processIt() {
        timer.onExpired()
    }

}