package core.simulator

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*

/**
 * Created on 22-07-2017

 * @author David Fialho
 */
object RandomDelayGeneratorTests : Spek({

    context("a random delay generator that has already generated 3 delays") {

        val generator = RandomDelayGenerator.with(min = 0, max = 10)
        val delays = listOf(generator.nextDelay(), generator.nextDelay(), generator.nextDelay())

        given ("reset is called") {

            generator.reset()

            on("calling nextDelay 3 times") {

                val delaysAfterReset = listOf(generator.nextDelay(), generator.nextDelay(), generator.nextDelay())

                it("generates the same 3 delays as before in the exact same order") {
                    assertThat(delaysAfterReset, `is`(delays))
                }
            }
        }

    }

    given("a random delay generator with a minimum of 1 and a maximum of 5") {

        val generator = RandomDelayGenerator.with(min = 1, max = 5)

        on ("generating 100 delays values") {

            val delays = (1..100).map { generator.nextDelay() }.toList()

            it("generates all values between 1 and 5 (inclusive)") {
                delays.forEach { assertThat(it, allOf(greaterThanOrEqualTo(1), lessThanOrEqualTo(5))) }
            }

            it("generates some delays of 5") {
                assertThat(delays, hasItem(5))
            }

            it("generates some delays of 1") {
                assertThat(delays, hasItem(1))
            }

        }

    }

})
