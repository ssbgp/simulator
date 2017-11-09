package simulation

import core.routing.Route

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 *
 * FIXME add some documentation here
 */
interface Runner<R: Route> {

    /**
     * Runs the specified execution.
     */
    fun run(execution: Execution<R>)

}