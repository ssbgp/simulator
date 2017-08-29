package core.simulator2

import core.simulator.Event
import core.simulator.Scheduler
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.assertThrows

/**
 * Created on 22-07-2017

 * @author David Fialho
 */
object SchedulerTests : Spek({

    given("an empty scheduler that has never had an event") {

        beforeGroup {
            Scheduler.reset()
        }

        it("has no events") {
            assertThat(Scheduler.hasEvents(), `is`(false))
        }

        it("throws an exception when trying to get the next event") {
            assertThrows(NoSuchElementException::class.java) { ->
                Scheduler.nextEvent()
            }
        }

        it("has time of 0") {
            assertThat(Scheduler.time, equalTo(0))
        }

        on("scheduling a new event to occur at time 10") {

            Scheduler.schedule(event(id = 0), timestamp = 10)

            it("has events") {
                assertThat(Scheduler.hasEvents(), `is`(true))
            }

            it("it returns the scheduled event when trying to get the next event") {
                assertThat(Scheduler.nextEvent(), `is`(event(id = 0)))
            }

            it("has time of 10") {
                assertThat(Scheduler.time, `is`(10))
            }

            it("no longer has events in the queue") {
                assertThat(Scheduler.hasEvents(), `is`(false))
            }

        }

        on("scheduling an event at time 15") {

            Scheduler.schedule(event(id = 1), timestamp = 15)
        }

        on("and scheduling another event at time 20") {

            Scheduler.schedule(event(id = 2), timestamp = 20)

            it("has events") {
                assertThat(Scheduler.hasEvents(), `is`(true))
            }

            it("has time of 10") {
                assertThat(Scheduler.time, `is`(10))
            }
        }

        on("trying to get the next event") {

            it("returns the event scheduled at time 15") {
                assertThat(Scheduler.nextEvent(), `is`(event(id = 1)))
            }

            it("still has events") {
                assertThat(Scheduler.hasEvents(), `is`(true))
            }

            it("has time of 15") {
                assertThat(Scheduler.time, `is`(15))
            }

        }

        on("scheduling another event at time 10") {

            it("throws an IllegalArgumentException") {
                assertThrows(IllegalArgumentException::class.java) { ->
                    Scheduler.schedule(event(id = 3), timestamp = 10)
                }
            }
        }

        on("scheduling another event at time 15: equal to the current time") {

            it("does not throw any exception") {
                Scheduler.schedule(event(id = 4), timestamp = 15)
            }
        }
    }



    given("a scheduler with time 10 and containing an event to occur at time 20") {

        beforeGroup {
            Scheduler.reset()
        }

        Scheduler.schedule(event(id = 0), timestamp = 10)
        Scheduler.nextEvent()   // remove the event with timestamp 10, which updates the Scheduler time to 10
        Scheduler.schedule(event(id = 1), timestamp = 20)

        on("scheduling an event at time 15 and trying to get the next event") {

            Scheduler.schedule(event(id = 2), timestamp = 15)

            it("returns the event scheduled to time 15") {
                assertThat(Scheduler.nextEvent(), `is`(event(id = 2)))
            }

            it("has time 15") {
                assertThat(Scheduler.time, `is`(15))
            }
        }

    }

})

//region Fake implementation of an Event

/**
 * Fake implementation of an event used for testing the Scheduler.
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
