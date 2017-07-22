package core.simulator

import core.routing.Message
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 */
object ExporterTests : Spek({

    given("an exporter using a random delay generator") {

        val exporter = Exporter(RandomDelayGenerator.with(min = 1, max = 10, seed = 10L))

        on("exporting 100 messages") {

            it("keeps the deliver time of each message higher than the previous one") {

                var previousDeliverTime = 0
                var deliverTime = exporter.export(message())

                for (i in 1..99) {
                    assertThat(deliverTime, greaterThan(previousDeliverTime))

                    previousDeliverTime = deliverTime
                    deliverTime = exporter.export(message())
                }
            }
        }
    }

})

//region Fake implementation of message used in the tests

object FakeMessage : Message {
    override fun send() {
        throw UnsupportedOperationException("Not necessary for the tests")
    }
}

/**
 * Returns a message.
 */
fun message(): Message {
    return FakeMessage
}

//endregion