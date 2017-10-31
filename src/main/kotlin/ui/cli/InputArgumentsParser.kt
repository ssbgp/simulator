package ui.cli

import bgp.BGP
import core.simulator.Engine
import core.simulator.RandomDelayGenerator
import io.InterdomainTopologyReaderHandler
import io.parseInterdomainExtender
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import simulation.*
import java.io.File
import java.util.*

/**
 * Created on 30-08-2017
 *
 * @author David Fialho
 */
class InputArgumentsParser {

    //region Options

    private val VERSION = "version"
    private val TOPOLOGY_FILE = "topology"
    private val DESTINATION = "destination"
    private val REPETITIONS = "repetitions"
    private val REPORT_DIRECTORY = "output"
    private val MIN_DELAY = "mindelay"
    private val MAX_DELAY = "maxdelay"
    private val THRESHOLD = "threshold"
    private val SEED = "seed"
    private val STUBS = "stubs"
    private val NODE_REPORT = "reportnodes"

    private val options = Options()

    init {
        // setup the command options
        options.addOption("v", VERSION, false, "show the version number")
        options.addOption("t", TOPOLOGY_FILE, true, "path to topology file")
        options.addOption("d", DESTINATION, true, "ID of the destination")
        options.addOption("c", REPETITIONS, true, "number of repetitions")
        options.addOption("o", REPORT_DIRECTORY, true, "directory where to output results")
        options.addOption(MIN_DELAY, true, "minimum message delay (inclusive)")
        options.addOption(MAX_DELAY, true, "maximum message delay (inclusive)")
        options.addOption("th", THRESHOLD, true, "threshold value")
        options.addOption(SEED, true, "first seed used to generate message delays")
        options.addOption(STUBS, true, "path to stubs file")
        options.addOption("rn", NODE_REPORT, false, "output data for each individual node")
    }

    @Throws(InputArgumentsException::class)
    fun parse(args: Array<String>): Pair<Runner, Execution> {

        val commandLine = try {
            DefaultParser().parse(options, args)
        } catch (e: ParseException) {
            throw InputArgumentsException(e.message.toString())
        }

        if (commandLine.hasOption(VERSION)) {

            if (commandLine.options.size > 1) {
                throw InputArgumentsException("when option -v/-version is specified, no more options are expected ")
            }

            println("SS-BGP Simulator: ${Engine.version()}")

            System.exit(0)
        }

        commandLine.let {

            val topologyFile = getFile(it, option = TOPOLOGY_FILE).get()
            val destination = getNonNegativeInteger(it, option = DESTINATION)
            val repetitions = getPositiveInteger(it, option = REPETITIONS, default = 1)
            val reportDirectory = getDirectory(it, option = REPORT_DIRECTORY, default = File(System.getProperty("user.dir")))
            val threshold = getPositiveInteger(it, option = THRESHOLD, default = 1_000_000)
            val seed = getLong(it, option = SEED, default = System.currentTimeMillis())
            val stubsFile = getFile(it, option = STUBS, default = Optional.empty())

            val minDelay = getPositiveInteger(it, option = MIN_DELAY, default = 1)
            val maxDelay = getPositiveInteger(it, option = MAX_DELAY, default = 1)

            if (maxDelay < minDelay) {
                throw InputArgumentsException("Maximum delay must equal to or greater than the minimum delay: " +
                        "min=$minDelay > max=$maxDelay")
            }

            // If the topology filename is `topology.nf` and the destination is 10 the report filename
            // is `topology_10.basic.csv`
            val outputName = topologyFile.nameWithoutExtension

            val basicReportFile = File(reportDirectory, outputName.plus("_$destination.basic.csv"))
            val nodesReportFile = File(reportDirectory, outputName.plus("_$destination.nodes.csv"))
            val metadataFile = File(reportDirectory, outputName.plus("_$destination.meta.txt"))

            val topologyReader = InterdomainTopologyReaderHandler(topologyFile)
            val messageDelayGenerator = RandomDelayGenerator.with(minDelay, maxDelay, seed)

            val stubDB = if (stubsFile.isPresent) {
                StubDB(stubsFile.get(), BGP(), ::parseInterdomainExtender)
            } else {
                null
            }

            val runner = RepetitionRunner(
                    topologyFile,
                    topologyReader,
                    destination,
                    repetitions,
                    messageDelayGenerator,
                    stubDB,
                    threshold,
                    metadataFile
            )

            val execution = SimpleAdvertisementExecution().apply {
                dataCollectors.add(BasicDataCollector(basicReportFile))

                if (commandLine.hasOption(NODE_REPORT)) {
                    dataCollectors.add(NodeDataCollector(nodesReportFile))
                }
            }

            return Pair(runner, execution)
        }
    }
    @Throws(InputArgumentsException::class)
    private fun getFile(commandLine: CommandLine, option: String, default: Optional<File>? = null): Optional<File> {
        verifyOption(commandLine, option, default)

        val value = commandLine.getOptionValue(option)
        val file = if (value != null) Optional.of(File(value)) else default!!   // See note below
        // Note: the verifyOption method would throw exception if the option was ot defined and default was null

        if (file.isPresent && !file.get().isFile) {
            throw InputArgumentsException("The file specified for `$option` does not exist: ${file.get().path}")
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
            throw InputArgumentsException("The directory specified for `$option` does not exist: ${directory.path}")
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
            throw InputArgumentsException("Parameter '$option' must be a non-negative integer value: was '$value'")
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
            throw InputArgumentsException("Parameter '$option' must be a positive integer value: was '$value'")
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
            throw InputArgumentsException("Parameter '$option' must be a positive long value: was '$value'")
        }
    }

    @Throws(InputArgumentsException::class)
    private fun verifyOption(commandLine: CommandLine, option: String, default: Any?) {

        if (!commandLine.hasOption(option) && default == null) {
            throw InputArgumentsException("The parameter '$option' is missing and it is mandatory")
        }
    }
}
