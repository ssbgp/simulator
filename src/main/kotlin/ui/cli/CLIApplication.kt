package ui.cli

import core.routing.Node
import core.routing.NodeID
import core.routing.Route
import core.routing.Topology
import core.simulator.Advertisement
import core.simulator.Engine
import io.ParseException
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
            console.error("Input arguments are invalid.")
            console.error("Cause: ${e.message ?: "No information available."}")
            console.info("Try the '-h' option to see more information")
            exitProcess(1)

        } catch (e: Exception) {
            console.error("Program was interrupted due to unexpected error: ${e.javaClass.simpleName}")
            console.error("Cause: ${e.message ?: "No information available."}")
            exitProcess(1)
        }
    }

    /**
     * Invoked while loading the topology.
     *
     * @param topologyFile   the file from which the topology will be loaded
     * @param loadBlock      the code block to load the topology.
     */
    override fun <R: Route> loadTopology(topologyFile: File, loadBlock: () -> Topology<R>): Topology<R> {

        try {
            console.info("Topology file: ${topologyFile.path}.")
            console.info("Loading topology...  ", inline = true)

            val (duration, topology) = timer {
                loadBlock()
            }

            console.print("loaded in $duration seconds")
            return topology

        } catch (exception: ParseException) {
            console.error("Failed to load topology due to parse error.")
            console.error("Cause: ${exception.message ?: "No information available"}")
            exitProcess(1)

        } catch (exception: IOException) {
            console.error("Failed to load topology due to IO error.")
            console.error("Cause: ${exception.message ?: "No information available"}")
            exitProcess(2)
        }

    }

    /**
     * Invoked when trying to find the destination node based on the ID.
     *
     * @param destinationID the destination ID
     * @param block         the block of code to find the destination
     */
    override fun <R: Route> findDestination(destinationID: NodeID, block: () -> Node<R>?): Node<R> {

        val destination = try {
            block()

        } catch (exception: ParseException) {
            console.error("Failed to parse stubs file.")
            console.error("Cause: ${exception.message ?: "No information available"}")
            exitProcess(1)

        } catch (exception: IOException) {
            console.error("Failed to read stubs file due to IO error.")
            console.error("Cause: ${exception.message ?: "No information available"}")
            exitProcess(2)
        }

        if (destination == null) {
            console.error("Destination `$destinationID` was not found.")
            exitProcess(3)
        }

        return destination
    }

    /**
     * Invoked while executing each execution.
     *
     * @param executionID  the identifier of the execution
     * @param advertisement  the destination used in the execution
     * @param seed         the seed of the message delay generator used for the execution
     * @param executeBlock the code block that performs one execution
     */
    override fun <R: Route> execute(executionID: Int, advertisement: Advertisement<R>, seed: Long,
                                    executeBlock: () -> Unit) {

        console.info("Executing $executionID (seed=$seed)...  ", inline = true)
        val (duration, _) = timer {
            executeBlock()
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
            console.info("Finished run in $duration in seconds")

        } catch (exception: IOException) {
            console.error("Failed to report results due to an IO error.")
            console.error("Cause: ${exception.message ?: "No information available"}")
            exitProcess(4)
        }

    }

}

private fun <R> timer(block: () -> R): Pair<Double, R> {

    val start = Instant.now()
    val value = block()
    val end = Instant.now()

    return Pair(Duration.between(start, end).toMillis().div(1000.0), value)
}