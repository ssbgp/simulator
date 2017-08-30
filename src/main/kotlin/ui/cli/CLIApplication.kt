package ui.cli

import core.routing.Node
import core.routing.NodeID
import core.routing.Topology
import io.ParseException
import io.TopologyReaderHandler
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
            val (runner, execution) = InputArgumentsParser().parse(args)
            runner.run(execution, this)

        } catch (e: InputArgumentsException) {
            console.error("Input arguments are invalid.\n${e.message}.")
            console.error("Cause: ${e.message ?: "No information available"}")

        } catch (e: Exception){
            console.error("Program was interrupted due to unexpected error.")
            console.error("Cause: ${e.message ?: "No information available"}")
        }
    }

    /**
     * Invoked while loading the topology.
     *
     * @param topologyFile   the file from which the topology will be loaded
     * @param topologyReader the reader used to load the topology into memory
     * @param loadBlock      the code block to load the topology.
     */
    override fun loadTopology(topologyFile: File, topologyReader: TopologyReaderHandler,
                              loadBlock: () -> Topology<*>): Topology<*> {

        try {
            console.info("Topology file: ${topologyFile.path}.")
            console.info("Loading topology...")

            val (duration, topology) = timer {
                loadBlock()
            }

            console.info("Topology loaded in $duration seconds")
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
    override fun findDestination(destinationID: NodeID, block: () -> Node<*>?): Node<*> {
        val destination: Node<*>? = block()

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
     * @param destination  the destination used in the execution
     * @param seed         the seed of the message delay generator used for the execution
     * @param executeBlock the code block that performs one execution
     */
    override fun execute(executionID: Int, destination: Node<*>, seed: Long, executeBlock: () -> Unit) {

        console.info("Executing $executionID... (destination=$destination and seed=$seed)")
        val (duration, _) = timer {
            executeBlock()
        }
        console.info("Finished $executionID in $duration seconds")
    }

    /**
     * Invoked during a run.
     */
    override fun run(runBlock: () -> Unit) {

        try {
            console.info("Running...")
            runBlock()
            console.info("Finished run")

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