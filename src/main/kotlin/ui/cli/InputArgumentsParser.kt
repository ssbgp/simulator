package ui.cli

import core.simulator.RandomDelayGenerator
import io.InterdomainTopologyReaderHandler
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import simulation.*
import java.io.File

/**
 * Created on 30-08-2017
 *
 * @author David Fialho
 */
class InputArgumentsParser {

    //region Options

    private val TOPOLOGY_FILE = "topology"
    private val DESTINATION = "destination"
    private val REPETITIONS = "repetitions"
    private val REPORT_DIRECTORY = "output"
    private val MIN_DELAY = "mindelay"
    private val MAX_DELAY = "maxdelay"
    private val THRESHOLD = "threshold"
    private val SEED = "seed"

    private val options = Options()

    init {
        // setup the command options
        options.addOption("t", TOPOLOGY_FILE, true, "path to topology file")
        options.addOption("d", DESTINATION, true, "ID of the destination")
        options.addOption("c", REPETITIONS, true, "number of repetitions")
        options.addOption("o", REPORT_DIRECTORY, true, "directory where to output results")
        options.addOption(MIN_DELAY, true, "minimum message delay (inclusive)")
        options.addOption(MAX_DELAY, true, "maximum message delay (inclusive)")
        options.addOption("th", THRESHOLD, true, "threshold value")
        options.addOption(SEED, true, "first seed used for generate message delays")
    }

    @Throws(InputArgumentsException::class)
    fun parse(args: Array<String>): Pair<Runner, Execution> {

        DefaultParser().parse(options, args).let {

            val topologyFile = getFile(it, option = TOPOLOGY_FILE)
            val destination = getNonNegativeInteger(it, option = DESTINATION)
            val repetitions = getPositiveInteger(it, option = REPETITIONS, default = 1)
            val reportDirectory = getDirectory(it, option = REPORT_DIRECTORY, default = File(System.getProperty("user.dir")))
            val threshold = getPositiveInteger(it, option = THRESHOLD, default = 1_000_000)
            val seed = getLong(it, option = SEED, default = System.currentTimeMillis())

            val minDelay = getPositiveInteger(it, option = MIN_DELAY, default = 1)
            val maxDelay = getPositiveInteger(it, option = MAX_DELAY, default = 1)

            if (maxDelay < minDelay) {
                throw InputArgumentsException("Maximum delay must equal to or greater than the minimum delay: " +
                        "min=$minDelay > max=$maxDelay")
            }

            // If the topology filename is `topology.nf` and the destination is 10 the report filename
            // is `topology_10.basic.csv`
            val reportFile = File(reportDirectory, topologyFile.nameWithoutExtension.plus("_$destination.basic.csv"))
            val topologyReader = InterdomainTopologyReaderHandler(topologyFile)
            val messageDelayGenerator = RandomDelayGenerator.with(minDelay, maxDelay, seed)

            val runner = RepetitionRunner(topologyFile, topologyReader, destination, repetitions, messageDelayGenerator)
            val execution = SimpleAdvertisementExecution(threshold).apply {
                dataCollectors.add(BasicDataCollector(reportFile))
            }

            return Pair(runner, execution)
        }
    }

    @Throws(InputArgumentsException::class)
    private fun getFile(commandLine: CommandLine, option: String, default: File? = null): File {
        verifyOption(commandLine, option, default)

        val value = commandLine.getOptionValue(option)
        val file = if (value != null) File(value) else default!!    // See note below

        // Note: the verifyOption method would throw exception if the option was ot defined and default was null

        if (!file.isFile) {
            throw InputArgumentsException("File '${file.path}' specified for option '$option' does not exist")
        }

        return file
    }

    @Throws(InputArgumentsException::class)
    private fun getDirectory(commandLine: CommandLine, option: String, default: File? = null): File {
        verifyOption(commandLine, option, default)

        val value = commandLine.getOptionValue(option)
        val directory = if (value != null) File(value) else default!!    // See note below

        // Note: the verifyOption method would throw exception if the option was ot defined and default was null

        if (!directory.isDirectory) {
            throw InputArgumentsException("Directory '${directory.path}' specified for option '$option' does not exist")
        }

        return directory
    }

    @Throws(InputArgumentsException::class)
    private fun getNonNegativeInteger(commandLine: CommandLine, option: String, default: Int? = null): Int {
        verifyOption(commandLine, option, default)

        val value = commandLine.getOptionValue(option)

        try {
            val intValue = value?.toInt() ?: default!!  // See note below
            // Note: the verifyOption method would throw exception if the option was ot defined and default was null

            if (intValue < 0) {
                // Handle error in th
                throw NumberFormatException()
            }

            return intValue

        } catch (numberError: NumberFormatException) {
            throw InputArgumentsException("Option '$option' must be a non-negative integer value: was '$value'")
        }
    }

    @Throws(InputArgumentsException::class)
    private fun getPositiveInteger(commandLine: CommandLine, option: String, default: Int? = null): Int {
        verifyOption(commandLine, option, default)

        val value = commandLine.getOptionValue(option)

        try {
            val intValue = value?.toInt() ?: default!!  // See note below
            // Note: the verifyOption method would throw exception if the option was ot defined and default was null

            if (intValue <= 0) {
                // Handle error in th
                throw NumberFormatException()
            }

            return intValue

        } catch (numberError: NumberFormatException) {
            throw InputArgumentsException("Option '$option' must be a positive integer value: was '$value'")
        }
    }

    @Throws(InputArgumentsException::class)
    private fun getLong(commandLine: CommandLine, option: String, default: Long? = null): Long {
        verifyOption(commandLine, option, default)

        val value = commandLine.getOptionValue(option)

        try {
            return value?.toLong() ?: default!!  // See note below
            // Note: the verifyOption method would throw exception if the option was ot defined and default was null

        } catch (numberError: NumberFormatException) {
            throw InputArgumentsException("Option '$option' must be a positive long value: was '$value'")
        }
    }

    @Throws(InputArgumentsException::class)
    private fun verifyOption(commandLine: CommandLine, option: String, default: Any?) {

        if (!commandLine.hasOption(option) && default == null) {
            throw InputArgumentsException("The option '$option' is missing and it is mandatory")
        }
    }
}
