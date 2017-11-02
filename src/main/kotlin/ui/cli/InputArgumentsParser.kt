package ui.cli

import bgp.BGP
import core.simulator.Engine
import core.simulator.RandomDelayGenerator
import io.InterdomainTopologyReaderHandler
import io.parseInterdomainExtender
import org.apache.commons.cli.*
import simulation.*
import java.io.File
import java.util.*
import kotlin.system.exitProcess


/**
 * Created on 30-08-2017
 *
 * @author David Fialho
 */
class InputArgumentsParser {

    //region Options

    // Information Options
    private val HELP = "help"
    private val VERSION = "version"

    // Execution Options
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

        options.apply {

            // Information Options
            addOption(Option.builder("h")
                    .desc("Print help")
                    .required(false)
                    .hasArg(false)
                    .longOpt(HELP)
                    .build())
            addOption(Option.builder("V")
                    .desc("Print application's version")
                    .required(false)
                    .hasArg(false)
                    .longOpt(VERSION)
                    .build())

            // Execution Options
            addOption(Option.builder("t")
                    .desc("Topology file to simulate with")
                    .hasArg(true)
                    .argName("topology-file")
                    .longOpt(TOPOLOGY_FILE)
                    .build())
            addOption(Option.builder("d")
                    .desc("ID of destination node")
                    .hasArg(true)
                    .argName("destination")
                    .longOpt(DESTINATION)
                    .build())
            addOption(Option.builder("c")
                    .desc("Number of executions to run [default: 1]")
                    .hasArg(true)
                    .argName("executions")
                    .longOpt(REPETITIONS)
                    .build())
            addOption(Option.builder("o")
                    .desc("Directory to place reports [default: working directory]")
                    .hasArg(true)
                    .argName("out-directory")
                    .longOpt(REPORT_DIRECTORY)
                    .build())
            addOption(Option.builder("min")
                    .desc("Minimum delay applied to the routing messages [default: 1]")
                    .hasArg(true)
                    .argName("mindelay")
                    .longOpt(MIN_DELAY)
                    .build())
            addOption(Option.builder("max")
                    .desc("Maximum delay applied to the routing messages [default: 1]")
                    .hasArg(true)
                    .argName("maxdelay")
                    .longOpt(MAX_DELAY)
                    .build())
            addOption(Option.builder("th")
                    .desc("Maximum amount simulation time [default: 1000000]")
                    .hasArg(true)
                    .argName("threshold")
                    .longOpt(THRESHOLD)
                    .build())
            addOption(Option.builder("s")
                    .desc("Seed used to generate the delays in the first execution")
                    .required(false)
                    .hasArg(true)
                    .argName("threshold")
                    .longOpt(SEED)
                    .build())
            addOption(Option.builder("S")
                    .desc("Stubs file")
                    .required(false)
                    .hasArg(true)
                    .argName("stubs-file")
                    .longOpt(STUBS)
                    .build())
            addOption(Option.builder("rn")
                    .desc("Output data for each individual node")
                    .required(false)
                    .hasArg(false)
                    .longOpt(NODE_REPORT)
                    .build())
        }

    }

    @Throws(InputArgumentsException::class)
    fun parse(args: Array<String>): Pair<Runner, Execution> {

        val commandLine = try {
            DefaultParser().parse(options, args)
        } catch (e: ParseException) {
            throw InputArgumentsException(e.message.toString())
        }

        if (commandLine.hasOption(VERSION)) {
            println("SS-BGP Simulator: ${Engine.version()}")
            exitProcess(0)
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
