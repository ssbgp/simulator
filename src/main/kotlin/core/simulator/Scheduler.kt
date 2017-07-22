package core.simulator

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 */
class Scheduler {

    /**
     * A scheduled event associates a timestamp with an event. The timestamp is used by the scheduler to determine
     * the order in which the event occur.
     */
    private data class ScheduledEvent(val time: Time, val event: Event) : Comparable<Time> {
        override fun compareTo(other: Time): Int {
            TODO("not implemented")
        }
    }

    /**
     * Always indicates the current time. It updates every time a new event is taken from the scheduler.
     */
    var time: Time = 0
        private set

    /**
     * Schedules an event to occur in the specified timestamp.
     */
    fun schedule(event: Event, timestamp: Time) {

    }

    /**
     * Schedules an event to occur 'interval' units of time from the current time.
     */
    fun scheduleFromNow(event: Event, interval: Time) {

    }

    /**
     * Returns true if the scheduler still has events in the queue or false if otherwise.
     */
    fun hasEvents(): Boolean {
        return false
    }

    /**
     * Returns the next event in the queue. It ay update the current time.
     *
     * @throws NoSuchElementException if the scheduler has no more events in the queue.
     */
    @Throws(NoSuchElementException::class)
    fun nextEvent(): Event {
        TODO("not implemented yet")
    }

}