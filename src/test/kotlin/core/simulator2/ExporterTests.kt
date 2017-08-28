package core.simulator2

import core.routing2.Message
import core.routing2.Route
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import testing2.invalidRoute
import testing2.node
import testing2.someExtender

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 */
object ExporterTests : Spek({

    /**
     * Returns a message.
     */
    fun message(): Message<Route> {
        return Message(node(1), node(2), invalidRoute(), someExtender())
    }

    given("an exporter using a random delay generator") {

        // Change message delay generator to random generator
        Engine.messageDelayGenerator = RandomDelayGenerator.with(min = 1, max = 10, seed = 10L)

        afterGroup {
            Engine.resetToDefaults()
        }

        val exporter = Exporter<Route>()

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

