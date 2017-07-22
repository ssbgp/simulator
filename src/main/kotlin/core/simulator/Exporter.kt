package core.simulator

import core.routing.Message

/**
 * Created on 22-07-2017
 *
 * @author David Fialho
 */
interface Exporter {

    /**
     * It issues an export event with the specified message.
     */
    fun export(message: Message)
}