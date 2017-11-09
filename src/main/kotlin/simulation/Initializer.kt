package simulation

import core.routing.Route
import ui.Application

/**
 * Created on 09-11-2017
 *
 * @author David Fialho
 *
 * This is just a tag interface for Initializers.
 *
 * An initializer is responsible for setting up the simulator and getting it ready to run. To do so, it may require
 * some parameters that should be provided in the constructor.
 *
 * FIXME this documentation is a work in progress
 */
interface Initializer<R: Route> {

    /**
     * Initializes a runner and execution based on some predefined parameters.
     */
    fun initialize(application: Application, metadata: Metadata): Pair<Runner<R>, Execution<R>>
}