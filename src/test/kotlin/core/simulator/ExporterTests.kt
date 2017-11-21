package core.simulator

import core.routing.Message
import core.routing.Route
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import testing.invalidRoute
import testing.node
import testing.someExtender

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 */
object ExporterTests : Spek({

    /**
     * Returns a message, any message.
     */
    fun message(): Message<Route> = Message(node(1), node(2), invalidRoute(), someExtender())

    given("a new exporter using a RandomDelayGenerator") {

        afterGroup {
            // Ensure engine is reset to default after finishing the test group
            Engine.resetToDefaults()
        }

        val exporter = Exporter<Route>()
        Engine.messageDelayGenerator = RandomDelayGenerator.with(min = 1, max = 10, seed = 10L)

        on("exporting 100 messages") {

            val times = ArrayList<Int>()

            for (i in 1..100) {
                times.add(exporter.export(message()))
            }

            it("keeps the deliver time of each message higher than the previous one") {

                for (i in 1..99) {
                    assertThat(times[i], greaterThan(times[i - 1]))
                }
            }
        }
    }

})
