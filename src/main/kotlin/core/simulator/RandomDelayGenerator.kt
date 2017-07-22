package core.simulator

import java.lang.System
import java.util.Random

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 *
 * Generates delays randomly in a specified interval.
 *
 * @param min  the minimum delay value the generator will generate
 * @param max  the maximum delay value the generator will generate
 * @param seed the seed used to generate the random delays
 */
class RandomDelayGenerator
private constructor(val min: Time, val max: Time, override val seed: Long)
    : DelayGenerator {

    companion object Factories {

        /**
         * Returns a RandomDelayGenerator with the specified configurations.
         *
         * @param min  the minimum delay value the generator will generate. Must be higher than 0
         * @param max  the maximum delay value the generator will generate. Must be higher than or equal to 'min'
         * @param seed the seed used to generate the random delays. If none is provided it uses the system current time
         * @throws IllegalStateException if 'max' is lower than 'min' or if 'min' is lower than 0.
         */
        @Throws(IllegalStateException::class)
        fun with(min: Time, max: Time, seed: Long = System.currentTimeMillis()): RandomDelayGenerator {

            if (min < 0 || max < min) {
                throw IllegalArgumentException("Maximum delay can not be lower than minimum and minimum must be a " +
                        "non-negative value")
            }

            return RandomDelayGenerator(min, max, seed)
        }
    }

    /**
     * Random value generator used to generate the delays
     */
    private var random = Random(seed)

    override fun nextDelay(): Time = random.nextInt(max - min + 1) + min

    override fun reset(): Unit = random.setSeed(seed)

}