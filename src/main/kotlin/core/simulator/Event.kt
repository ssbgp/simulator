package core.simulator

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 *
 * Interface for simulation event.
 */
interface Event {

    /**
     * Processes this event. This method is called at the time the event is supposed to be
     * processed. Subclasses should use this method to implement whatever action this event
     * triggers.
     */
    fun processIt()
}