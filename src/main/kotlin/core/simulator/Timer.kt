package core.simulator

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
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

    }

    /**
     * Should be called when the timer expires. It calls the action and sets the timer as 'expired'.
     */
    fun onExpired() {

    }

}