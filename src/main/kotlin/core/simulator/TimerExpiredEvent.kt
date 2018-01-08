package core.simulator

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 *
 * A [TimerExpiredEvent] is used to implement the timer. It is issued when the timer starts and
 * it occurs (it is processed) when the timer expires. It triggers the [timer]'s [Timer.onExpired]
 * method.
 */
class TimerExpiredEvent(private val timer: Timer) : Event {

    override fun processIt() {
        timer.onExpired()
    }

}