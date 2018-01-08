package core.simulator

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.junit.jupiter.api.Assertions.assertThrows
import testing.thenOn
import org.hamcrest.Matchers.`is` as Is

/**
 * Created on 22-07-2017

 * @author David Fialho
 */
object SchedulerTests : Spek({

    given("a new scheduler") {

        val scheduler = Scheduler()

        it("has no events") {
            assertThat(scheduler.hasEvents(),
                    Is(false))
        }

        it("throws an exception when trying to get the next event") {
            assertThrows(NoSuchElementException::class.java) {
                scheduler.nextEvent()
            }
        }

        it("has time 0") {
            assertThat(scheduler.time, equalTo(0))
        }

        thenOn("scheduling an event for time 10") {

            scheduler.schedule(event(id = 0), timestamp = 10)

            it("has events") {
                assertThat(scheduler.hasEvents(),
                        Is(true))
            }
        }

        thenOn("getting the next event (1)") {

            val event = scheduler.nextEvent()

            it("returns the previously scheduled event") {
                assertThat(event,
                        Is(event(id = 0)))
            }

            it("has time of 10") {
                assertThat(scheduler.time, Is(10))
            }

            it("no longer has events in the queue") {
                assertThat(scheduler.hasEvents(),
                        Is(false))
            }
        }

        thenOn("scheduling two events 1 and 2 for times 15 and 20, respectively") {

            scheduler.schedule(event(id = 1), timestamp = 15)
            scheduler.schedule(event(id = 2), timestamp = 20)

            it("has events") {
                assertThat(scheduler.hasEvents(),
                        Is(true))
            }

            it("has time 10") {
                assertThat(scheduler.time,
                        Is(10))
            }
        }

        thenOn("getting the next event (2)") {

            it("returns the event 1 (scheduled at time 15)") {
                assertThat(scheduler.nextEvent(),
                        Is(event(id = 1)))
            }

            it("still has events") {
                assertThat(scheduler.hasEvents(),
                        Is(true))
            }

            it("has time of 15") {
                assertThat(scheduler.time, Is(15))
            }
        }

        thenOn("scheduling an event for time 12") {

            it("throws an IllegalArgumentException") {
                assertThrows(IllegalArgumentException::class.java) {
                    scheduler.schedule(event(id = 3), timestamp = 12)
                }
            }
        }

        thenOn("scheduling event 4 at time 15 (equal to the current time)") {

            it("does not throw any exception") {
                scheduler.schedule(event(id = 4), timestamp = 15)
            }
        }

        thenOn("getting the next event (3)") {

            it("returns event 4 (scheduled at time 15)") {
                assertThat(scheduler.nextEvent(),
                        Is(event(id = 4)))
            }

            it("still has events") {
                assertThat(scheduler.hasEvents(),
                        Is(true))
            }

            it("has time of 15") {
                assertThat(scheduler.time,
                        Is(15))
            }
        }

        thenOn("getting the next event (4)") {

            it("returns event 2 (scheduled at time 20)") {
                assertThat(scheduler.nextEvent(),
                        Is(event(id = 2)))
            }

            it("no longer has events") {
                assertThat(scheduler.hasEvents(),
                        Is(false))
            }

            it("has time of 20") {
                assertThat(scheduler.time,
                        Is(20))
            }
        }

    }

    given("a scheduler with time 10 and containing an event to occur at time 20") {

        val scheduler = Scheduler()

        scheduler.schedule(event(id = 0), timestamp = 10)
        scheduler.nextEvent()   // removing the event with time 10 updates the Scheduler time to 10
        scheduler.schedule(event(id = 1), timestamp = 20)

        thenOn("scheduling an event at time 15 and trying to get the next event") {

            scheduler.schedule(event(id = 2), timestamp = 15)

            it("has still has time 10") {
                assertThat(scheduler.time,
                        Is(10))
            }
        }

        thenOn("getting the next event") {

            val event = scheduler.nextEvent()

            it("returns the event scheduled at time 15") {
                assertThat(event,
                        Is(event(id = 2)))
            }

            it("has time 15") {
                assertThat(scheduler.time, Is(15))
            }
        }
    }

})

//region Fake implementation of an Event

/**
 * Fake implementation of an event used for testing the scheduler.
 *
 * It defines an ID property that can be used to identify each event in the tests. It is specially useful in
 * assertion error reports.
 *
 * Notice this is a data class, which means the equals method will consider the ID to determine the equality.
 */
data class FakeEvent(val id: Int) : Event {
    override fun processIt() {
        throw UnsupportedOperationException("Does not need to implement this for the tests")
    }
}

/**
 * Returns an event with the specified ID.
 */
fun event(id: Int = 0): Event = FakeEvent(id)

//endregion
