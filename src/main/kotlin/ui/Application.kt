package ui

import core.routing.Route
import core.routing.Topology
import core.simulator.Advertisement
import java.io.File

/**
 * Created on 30-08-2017
 *
 * @author David Fialho
 */
interface Application {

    fun launch(args: Array<String>)

    /**
     * Invoked while loading the topology.
     *
     * @param topologyFile the file from which the topology will be loaded
     * @param block        the code block to load the topology
     */
    fun <R: Route> loadTopology(topologyFile: File, block: () -> Topology<R>): Topology<R>

    /**
     * Invoked when setting up the advertisements to occur in the simulation. This may imply accessing the filesystem,
     * which may throw some IO error.
     *
     * @param block the block of code to setup advertisements
     * @return a list containing the advertisements already setup
     */
    fun <R: Route> setupAdvertisements(block: () -> List<Advertisement<R>>): List<Advertisement<R>>

    /**
     * Invoked when reading the stubs file. It returns whatever the [block] returns.
     *
     * @param file  the stubs file that is going to be read, null indicates the file was not read.
     * @param block the block of code to read stubs file
     * @return whatever the [block] returns.
     */
    fun <T> readStubsFile(file: File?, block: () -> T): T

    /**
     * Invoked when reading the advertisements file. It returns whatever the [block] returns.
     *
     * @param file  the advertisements file that is going to be read
     * @param block the block of code to read stubs file
     * @return whatever the [block] returns.
     */
    fun <T> readAdvertisementsFile(file: File, block: () -> T): T

    /**
     * Invoked while executing each execution.
     *
     * @param executionID    the identifier of the execution
     * @param advertisements the advertisements programmed to occur in the simulation
     * @param seed           the seed of the message delay generator used for the execution
     * @param block          the code block that performs one execution
     */
    fun <R: Route> execute(executionID: Int, advertisements: List<Advertisement<R>>, seed: Long,
                           block: () -> Unit)

    /**
     * Invoked during a run.
     */
    fun run(runBlock: () -> Unit)

    /**
     * Invoked while metadata is being written to disk.
     *
     * @param file the file where the metadata is going to be written to
     */
    fun writeMetadata(file: File, block: () -> Unit)

}