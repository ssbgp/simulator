package io

import core.routing.Topology
import java.io.IOException

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 */
interface TopologyReaderHandler {

    /**
     * Reads the topology file associated with the handler on a new topology reader and then closes it down correctly
     * whether an exception is thrown or not.
     *
     * @return the topology read from the file.
     * @throws IOException - If an I/O error occurs
     * @throws ParseException - if a topology object can not be created due to incorrect representation
     */
    @Throws(IOException::class, ParseException::class)
    fun read(): Topology<*>
}