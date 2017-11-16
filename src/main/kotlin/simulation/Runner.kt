package simulation

import core.routing.Route

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 *
 * TODO @doc - add documentation for Runner
 */
interface Runner<R: Route> {

    /**
     * Runs the specified execution.
     */
    fun run(execution: Execution<R>, metadata: Metadata)

}