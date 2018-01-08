package core.simulator

import java.util.*

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 *
 * The [RandomDelayGenerator] generates random delays within an interval between [min] and [max]
 * (inclusive), according to an uniform distribution.
 */
class RandomDelayGenerator
private constructor(override val min: Time, override val max: Time, seed: Long): DelayGenerator {

    /**
     * Seed used to generate the sequence of delays.
     * When reset() is called this seed is reused.
     */
    override var seed = seed
        private set

    companion object Factories {

        /**
         * Returns a [RandomDelayGenerator] which generates delays between [min] and [max]
         * (inclusive). The value of [max] must be higher than or equal to [min]. Delays are
         * generated from [seed]. If no [seed] is specified, then the current time in
         * milliseconds is used.
         *
         * @throws IllegalArgumentException if [max] is lower than [min] or [min] is lower than 0
         */
        @Throws(IllegalStateException::class)
        fun with(min: Time, max: Time, seed: Long = System.currentTimeMillis()): RandomDelayGenerator {

            if (min < 0) {
                throw IllegalArgumentException("minimum must be a non-negative value, but was $min")
            }

            if (max < min) {
                throw IllegalArgumentException("maximum delay can not be lower than minimum ($max < $min)")
            }

            return RandomDelayGenerator(min, max, seed)
        }
    }

    /**
     * Random number generator used to generate the delays
     */
    private var random = Random(seed)

    /**
     * Generates the next delay value and returns it.
     */
    override fun nextDelay(): Time = random.nextInt(max - min + 1) + min


    /**
     * Resets the delay generator. As a result, after calling [reset] the generator will generate
     * the same sequence of delays it generated after being created.
     */
    override fun reset() = random.setSeed(seed)

    /**
     * Generates a new seed. From this point on, delays will be generated from the new seed.
     */
    override fun generateNewSeed() {
        seed = random.nextInt().toLong()
        random = Random(seed)
    }
}