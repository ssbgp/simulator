package core.routing

/**
 * Created on 24-07-2017.
 *
 * @author David Fialho
 */
interface Destination {

    /**
     * Announces the destination to neighbors.
     */
    fun announceItSelf()
}