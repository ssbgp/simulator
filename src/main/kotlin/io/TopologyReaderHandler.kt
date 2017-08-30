package io

import core.routing.Topology
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.Reader

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 */
sealed class TopologyReaderHandler {

    /**
     * Reads the topology file associated with the handler on a new topology reader and then closes it down correctly
     * whether an exception is thrown or not.
     *
     * @return the topology read from the file.
     * @throws IOException - If an I/O error occurs
     * @throws ParseException - if a topology object can not be created due to incorrect representation
     */
    @Throws(IOException::class, ParseException::class)
    abstract fun read(): Topology<*>

}

/**
 * Handler for InterdomainTopologyReader.
 */
class InterdomainTopologyReaderHandler(private val reader: Reader): TopologyReaderHandler() {

    constructor(topologyFile: File): this(FileReader(topologyFile))

    /**
     * Reads the topology file associated with the handler on a new topology reader and then closes it down correctly
     * whether an exception is thrown or not.
     *
     * @throws IOException - If an I/O error occurs
     * @throws ParseException - if a topology object can not be created due to incorrect representation
     */
    @Throws(IOException::class, ParseException::class)
    override fun read(): Topology<*> {

        InterdomainTopologyReader(reader).use {
            return it.read()
        }
    }
}
