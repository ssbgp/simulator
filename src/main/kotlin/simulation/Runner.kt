package simulation

import ui.Application

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 */
interface Runner {

    /**
     * Runs the specified execution.
     */
    fun run(execution: Execution, application: Application)

}