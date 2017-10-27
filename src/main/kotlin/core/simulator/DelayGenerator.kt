package core.simulator

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 */
interface DelayGenerator {

    /**
     * The seed used to generate the delays.
     */
    val seed: Long

    /**
     * Minimum delay that the generator will generate.
     */
    val min: Time

    /**
     * Maximum delay that the generator will generate.
     */
    val max: Time

    /**
     * Generates the next delay value and returns it.
     */
    fun nextDelay(): Time

    /**
     * Resets the delay generator.
     *
     * The general contract of reset is that it alters the state of the delay generator object so as to be in
     * exactly the same state as if it had just been created and set to use the specified seed. Thus, future calls to
     * nextDelay() should return the same values as if the generator was just created.
     */
    fun reset()

    /**
     * Generates a new seed for the delay generator and sets the new seed as the generator's seed.
     */
    fun generateNewSeed()

}