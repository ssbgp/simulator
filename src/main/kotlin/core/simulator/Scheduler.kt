package core.simulator

import java.util.*

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
    private class ScheduledEvent(val time: Time, val event: Event) : Comparable<ScheduledEvent> {
        override operator fun compareTo(other: ScheduledEvent): Int = time.compareTo(other.time)
    }

    private val events = PriorityQueue<ScheduledEvent>()

    /**
     * Always indicates the current time. It updates every time a new event is taken from the scheduler.
     */
    var time: Time = 0
        private set

    /**
     * Schedules an event to occur in the specified timestamp.
     *
     * @throws IllegalArgumentException if the specified timestamp is before the current time of the scheduler.
     */
    @Throws(IllegalArgumentException::class)
    fun schedule(event: Event, timestamp: Time) {

        if (timestamp < time) {
            throw IllegalArgumentException("Scheduling time '$timestamp' is lower than the current time '$time'")
        }

        events.add(ScheduledEvent(timestamp, event))
    }

    /**
     * Schedules an event to occur 'interval' units of time from the current time.
     */
    fun scheduleFromNow(event: Event, interval: Time) {
        schedule(event, time + interval)
    }

    /**
     * Returns true if the scheduler still has events in the queue or false if otherwise.
     */
    fun hasEvents(): Boolean = !events.isEmpty()

    /**
     * Returns the next event in the queue. It ay update the current time.
     *
     * @throws NoSuchElementException if the scheduler has no more events in the queue.
     */
    @Throws(NoSuchElementException::class)
    fun nextEvent(): Event {

        val scheduledEvent = events.poll() ?: throw NoSuchElementException("Scheduler has no more events in the queue")

        time = scheduledEvent.time
        return scheduledEvent.event
    }

    /**
     * Resets the scheduler to the initial state: with no events in the queue and time set to 0.
     */
    fun reset() {
        events.clear()
        time = 0
    }

}