package core.simulator

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 *
 * The [NoDelayGenerator] generates delay values of 0. That is, [nextDelay] always return 0.
 */
object NoDelayGenerator : DelayGenerator {

    override val seed = 0L
    override val min: Time = 0
    override val max: Time = 0

    override fun nextDelay(): Time = 0

    override fun reset() = Unit
    override fun generateNewSeed() = Unit

}