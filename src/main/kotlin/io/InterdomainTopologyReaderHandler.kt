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
class InterdomainTopologyReaderHandler(private val reader: Reader): TopologyReaderHandler {

    constructor(topologyFile: File): this(FileReader(topologyFile))

    /**
     * Reads the topology file associated with the handler on a new topology reader and then closes it down correctly
     * whether an exception is thrown or not.
     *
     * @IOException - If an I/O error occurs
     * @ParseException - if a topology object can not be created due to incorrect representation
     */
    @Throws(IOException::class, ParseException::class)
    override fun read(): Topology<*> {

        InterdomainTopologyReader(reader).use {
            return it.read()
        }
    }
}