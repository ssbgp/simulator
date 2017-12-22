package core.simulator

import java.util.*

// The time is represented by an integer value.
typealias Time = Int

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 *
 * The [Scheduler] is the most important component of an event-drive simulator. It is responsible
 * for storing all events that occur during the simulation and deliver them according to their
 * scheduling times.
 */
class Scheduler {

    /**
     * A scheduled event associates a timestamp with an event. Events are scheduled according to
     * this timestamp. Events with a lower timestamp come before events with an higher timestamp.
     */
    private class ScheduledEvent(val time: Time, val event: Event) : Comparable<ScheduledEvent> {
        override operator fun compareTo(other: ScheduledEvent): Int = time.compareTo(other.time)
    }

    /**
     * Priority queue that keeps all scheduled events ordered according to their timestamps.
     */
    private val events = PriorityQueue<ScheduledEvent>()

    /**
     * Keeps track of the current time. The scheduler's time corresponds to the time of the last
     * event taken from the scheduler. Before any event is taken from this scheduler, the time is 0.
     */
    var time: Time = 0
        private set

    /**
     * Schedules an [event] to occur at some time given by [timestamp].
     *
     * @throws IllegalArgumentException if [timestamp] is lower than current [time].
     */
    @Throws(IllegalArgumentException::class)
    fun schedule(event: Event, timestamp: Time) {

        if (timestamp < time) {
            throw IllegalArgumentException("scheduling time '$timestamp' is lower than the " +
                    "current time '$time'")
        }

        events.add(ScheduledEvent(timestamp, event))
    }

    /**
     * Schedules an [event] to occurs [interval] units of time from the current [time].
     */
    fun scheduleFromNow(event: Event, interval: Time) {
        schedule(event, time + interval)
    }

    /**
     * Checks whether or not the scheduler still has events in the queue.
     */
    fun hasEvents(): Boolean = !events.isEmpty()

    /**
     * Returns the next event in the queue. As a side effect, this also may update this
     * scheduler's current [time].
     *
     * @throws NoSuchElementException if the scheduler has no more events in the queue.
     */
    @Throws(NoSuchElementException::class)
    fun nextEvent(): Event {

        val scheduledEvent = events.poll() ?: throw NoSuchElementException("no more events in the queue")

        time = scheduledEvent.time
        return scheduledEvent.event
    }

    /**
     * Resets the scheduler. All events are removed from the queue and the time set back to 0.
     */
    fun reset() {
        events.clear()
        time = 0
    }

}