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
 * @param seed the seed used to generate the random delays. If none is provided it uses the system current time
 */
class RandomDelayGenerator(val min: Time, val max: Time, override val seed: Long = System.currentTimeMillis())
    : DelayGenerator {

    /**
     * Random value generator used to generate the delays
     */
    private var random = Random(seed)

    override fun nextDelay(): Time = random.nextInt(max - min + 1) + min

    override fun reset(): Unit = random.setSeed(seed)

}