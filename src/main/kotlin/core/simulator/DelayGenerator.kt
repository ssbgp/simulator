package core.simulator

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 *
 * Interface for delay generators. All delay generator classes should implement this interface.
 *
 * @property seed the seed used to generate the delays
 * @property min  the minimum delay value the generator will generate
 * @property max  the maximum delay value the generator will generate
 */
interface DelayGenerator {

    val seed: Long
    val min: Time
    val max: Time

    /**
     * Generates the next delay value and returns it.
     */
    fun nextDelay(): Time

    /**
     * Resets the delay generator. As a result, after calling [reset] the generator will generate
     * the same sequence of delays it generated after being created.
     */
    fun reset()

    /**
     * Generates a new seed. From this point on, delays will be generated from the new seed.
     */
    fun generateNewSeed()

}