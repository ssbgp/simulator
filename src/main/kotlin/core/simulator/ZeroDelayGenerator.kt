package core.simulator

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 *
 * Generates constant delay values of zero! The method nextDelay() always returns 0.
 */
class ZeroDelayGenerator : DelayGenerator {

    override val seed = 0L

    override fun nextDelay(): Time = 0

    override fun reset() = Unit

}