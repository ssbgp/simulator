package ui.cli

import core.routing.Route
import core.routing.Topology
import core.simulator.Advertisement
import core.simulator.Engine
import io.ParseException
import simulation.InitializationException
import simulation.Metadata
import ui.Application
import java.io.File
import java.io.IOException
import java.time.Duration
import java.time.Instant
import kotlin.system.exitProcess

/**
 * Created on 30-08-2017
 *
 * @author David Fialho
 */
object CLIApplication: Application {

    private val console = Console()

    override fun launch(args: Array<String>) {

        try {
            val initializer = InputArgumentsParser().parse(args)
            val metadata = Metadata(version = Engine.version())
            val (runner, execution) = initializer.initialize(this, metadata)
            runner.run(execution, metadata)

        } catch (e: InputArgumentsException) {
            console.error("Input arguments are invalid")
            console.error("Cause: ${e.message ?: "No information available"}")
            console.info("Try the '-h' option to see more information")
            exitProcess(1)

        } catch (e: InitializationException) {
            console.error("Initialization failed")
            console.error("Cause: ${e.message ?: "No information available"}")
            exitProcess(1)

        } catch (e: Exception) {
            console.error("Program was interrupted due to unexpected error: ${e.javaClass.simpleName}")
            console.error("Cause: ${e.message ?: "No information available"}")
            exitProcess(1)
        }
    }

    /**
     * Invoked while loading the topology.
     *
     * @param topologyFile   the file from which the topology will be loaded
     * @param block      the code block to load the topology.
     */
    override fun <R: Route> loadTopology(topologyFile: File,
                                         block: () -> Topology<R>): Topology<R> {

        try {
            console.info("Topology file: ${topologyFile.path}.")
            console.info("Loading topology...  ", inline = true)

            val (duration, topology) = timer {
                block()
            }

            console.print("loaded in $duration seconds")
            return topology

        } catch (exception: ParseException) {
            console.print() // must print a new line here
            console.error("Failed to load topology due to parse error.")
            console.error("Cause: ${exception.message ?: "No information available"}")
            exitProcess(1)

        } catch (exception: IOException) {
            console.print() // must print a new line here
            console.error("Failed to load topology due to IO error.")
            console.error("Cause: ${exception.message ?: "No information available"}")
            exitProcess(2)
        }

    }

    /**
     * Invoked when setting up the advertisements to occur in the simulation. This may imply accessing the filesystem,
     * which may throw some IO error.
     *
     * @param block the block of code to setup advertisements
     * @return a list containing the advertisements already setup
     */
    override fun <R: Route> setupAdvertisements(block: () -> List<Advertisement<R>>): List<Advertisement<R>> {

        try {
            console.info("Setting up advertisements...  ", inline = true)

            val (duration, advertisements) = timer {
                block()
            }

            console.print("done in $duration seconds")
            console.info("Advertising nodes: ${advertisements.map { it.advertiser.id }.joinToString()}")

            return advertisements

        } catch (exception: ParseException) {
            console.print() // must print a new line here
            console.error("Failed to parse stubs file.")
            console.error("Cause: ${exception.message ?: "No information available"}")
            exitProcess(1)

        } catch (exception: IOException) {
            console.print() // must print a new line here
            console.error("Failed to read stubs file due to IO error.")
            console.error("Cause: ${exception.message ?: "No information available"}")
            exitProcess(2)

        } catch (exception: InitializationException) {
            console.print() // must print a new line here
            console.error("Failed to initialize the simulation.")
            console.error("Cause: ${exception.message ?: "No information available"}")
            exitProcess(3)
        }
    }

    /**
     * Invoked while executing each execution.
     *
     * @param executionID    the identifier of the execution
     * @param advertisements the advertisements that will occur during the execution
     * @param seed           the seed of the message delay generator used for the execution
     * @param block          the code block that performs one execution
     */
    override fun <R: Route> execute(executionID: Int, advertisements: List<Advertisement<R>>,
                                    seed: Long, block: () -> Unit) {

        console.info("Executing $executionID (seed=$seed)...  ", inline = true)
        val (duration, _) = timer {
            block()
        }
        console.print("finished in $duration seconds")
    }

    /**
     * Invoked during a run.
     */
    override fun run(runBlock: () -> Unit) {

        try {
            console.info("Running...")
            val (duration, _) = timer {
                runBlock()
            }
            console.info("Finished run in $duration seconds")

        } catch (exception: IOException) {
            console.error("Failed to report results due to an IO error.")
            console.error("Cause: ${exception.message ?: "No information available"}")
            exitProcess(4)
        }

    }

    /**
     * Invoked while metadata is being written to disk.
     *
     * @param file the file where the metadata is going to be written to
     */
    override fun writeMetadata(file: File, block: () -> Unit) {

        try {
            console.info("Writing metadata...  ", inline = true)
            val (duration, _) = timer {
                block()
            }
            console.print("done in $duration seconds")

        } catch (exception: IOException) {
            console.print() // must print a new line here
            console.error("Failed to metadata due to an IO error.")
            console.error("Cause: ${exception.message ?: "No information available"}")
            exitProcess(4)
        }
    }

}


// TODO @refactor - move timer to a utils file
private fun <R> timer(block: () -> R): Pair<Double, R> {

    val start = Instant.now()
    val value = block()
    val end = Instant.now()

    return Pair(Duration.between(start, end).toMillis().div(1000.0), value)
}