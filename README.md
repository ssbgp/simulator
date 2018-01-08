# SS-BGP Routing Simulator

An event-driven routing simulator for Self-Stable BGP (SS-BGP). It is also capable to run the well known Border Gateway Protocol (BGP).

Next we provide a set of instructions on how to install and run the simulator. If you are a developer and want to contribute to project or build on it, read the developer manual available [here](/doc/developers.md).

## Prerequisites

The simulator is an application that runs on the Java VM. To run it requires the Java Runtime Environment (JRE) (version 8 or later) to be installed. Download the JRE (version 8) for your own operating system from [here](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).

## Installation

The simulator application is composed of a single, self-contained, JAR file. It does not need to be installed on the system. The only "installation" step required is downloading the application's JAR file. To do this,

1. Go to [releases](https://github.com/ssbgp/routing-simulator/releases) and download, from the latest release section, a file called `ssbgp-simulator-{version}.jar`, where `{version}` corresponds to the release version.

1. Assuming the JRE is properly installed, open up the command line and type the following command, replacing `<path-to-jar-file>` with the path to the JAR file previously downloaded.

        java -jar <path-to-jar-file> --version

    If everything went ok, the message `SS-BGP Simulator: {version}`  should be present in the 
    terminal prompt, where `{version}` corresponds to the version of the application that was 
    downloaded. If any issues occur, check out the [Troubleshooting](#troubleshooting) section for solutions to common problems.

## Basic Usage

The [Installation](#installation) section already showed how to run the simulator application using the `--version` option to show the simulator's version. This command is useful to determine if we are running the expected version of the simulator. But, it does not do much else.

**From this point on will assume the path to the simulator's JAR file is `simulator.jar`**

To perform simulations, the simulator requires at least (1) a topology file and (2) a destination.

1. The topology file describes the network to be simulated, that is, the set of nodes and the links between them. Not only that, it also specifies, for each node, the protocol and MRAI value used by that node. See the [Topology File](#topology-file) section to learn how to create these files;

2. A destination is a node in the network, that announces itself to all other nodes. Hence, this node is the starting point of the simulation.

The command to perform a single simulation is as follows.

    java -jar simulator.jar -t <topology-file> -d <destination>

The `<topology-file>` parameter should be replaced with the path to the topology file to be used in the simulation, and the `<destination>` parameter must substituted with an integer value corresponding to the ID of the node to behave as the destination.

The last command will execute a single simulation run for the specified destination. To have the simulator perform multiple simulation runs with a single command, you need to use the `-c` option followed by the number of runs that you want to execute.

    java -jar simulator.jar -t <topology-file> -d <destination> -c <repetitions>

#### Output

By default, the simulator outputs two files to the current working directory: a `.meta.txt` file and a `.basic.csv`.

The `-o` can be used to redirect the output to a different directory.

      java -jar simulator.jar -t <topology-file> -d <destination> -o <output-directory>

Replacing `<output-directory>` with the path to the directory where the simulator should store the output files.

The first file is a simple text file, which includes some information about the setup of the simulation, such as the version of the simulator, the name of the topology file, etc. The second file contains the actual output of the simulation, the data obtained from the simulation. It is a Comma Separated Values (CSV) file containing a table with 8 columns and multiple rows (excluding the header row). Each row shows data corresponding to each simulation run. We suggest opening this file with a spreadsheet application, such as MS Excel or LibreOffice's Calc.

| Note |
|:---|
| If the `-c N` option was used, than the table will have `N` rows (excluding the header row), one row per simulation run. If the `-c` option was not used, than only a single simulation run was performed and, therefore, the file will contain only a single row.* |

Each column in the table corresponds to a piece of information collected by the simulator during the execution of each simulation run. The table includes the following columns:

- **Simulation** - number identifying each simulation
- **Delay Seed** - seed used to generate the random message delays
- **Termination Time (Total)** - the termination time of the last node to terminate
- **Termination Time (Avg.)** - the termination time over all nodes
- **Message Count** - the total number of messages sent during the simulation
- **Detection Count** - the total number of detections/deactivations performed during the simulation
- **Terminated** - a flag indicating whether or not the simulation terminated before reaching the maximum threshold
- **Disconnected Count** - the number of nodes that were disconnected when the simulation terminated

**Note:** *time* in this context refers to simulation time and not processing (real) time.

#### How to ask for help?
Last but not least, the most important command in any command line tool is the one that can help you. To obtain some information about each option that is available, run the simulator with option `-h/--help`, as follows.

    java -jar simulator.jar --help

There are other useful options, not shown in this section, that may be of use. Those are described in the [Advanced Usage](#advanced-usage) section.

## Topology File

The topology file is a text file with a predefined format. The format is very simple. Each line includes a single key and multiple values associated with that key. Keys are separated from values with an equals sign `=` and values are separated between each other with a straight line `|`.

There are only two possible keys: `node` and `link`. The former describes a node, and the latter a link. Nodes have 3 values,

- an ID, that must be unique for each node;
- a protocol label
- and, an MRAI value.

The protocol label is one of BGP, SSBGP, ISSBGP, SSBGP2, and ISSBGP2. The specified label determines the protocol deployed by the node. This allows simulating with different nodes deploying different protocols.

Links connect two nodes in a single direction. Routing messages flow in the opposite direction of the links. Since links are unidirectional, it is possible to represent unidirectional connections, with routing messages traveling from a node A to a node B, but not from B to A. A link has 3 values. The first two values correspond to the IDs of the nodes connected by that link: the first ID corresponds to the link's tail and the second to its head. The third value is a relationship label. A relationship label describes how routes are transformed when they are imported from and exported through a link. Currently, 6 different labels are supported:

- R+ - peer+ relationship
- R* - peer* relationship
- C - customer relationship
- R - peer relationship
- P - provider relationship
- S - sibling relationship

*See [Relationships](#relationships) section to learn more about relationships.*

Here is an example of a topology file with 3 nodes and 4 links.

    node = 0 | BGP | 5000
    node = 1 | SSBGP | 3000
    node = 2 | SSBGP2 | 1000
    link = 1 | 0 | C
    link = 2 | 0 | C
    link = 1 | 2 | R+
    link = 2 | 1 | R+

Each node deploys a different protocol: node 0 deploys BGP, node 1 deploys SS-BGP, and node 2 deploys the SS-BGP version 2. The same is through for the MRAI values: node 0 uses an MRAI of 5000, while nodes 1 and 2 use MRAI values of 3000 and 1000, respectively.

There is a *customer link* from node 1 to node 0, and another one from node 2 to node 0. Nodes 1 and 2 are connected in both directions through peer+ links.

### Relationships
In progress...

## Advanced Usage

### How to change the minimum and maximum message delays?

The order with which routing messages are processed affects the routing behavior, thus affecting the overall development of the routing protocol. This order is defined by the delays imposed by the network and its components. The simulator aggregates all these delays in a single value that assigns to each routing message. These values are generated using an uniform distribution. Important to note that, regardless of any delays, routing messages sent across one link are always delivered in a first-in-first-out order. Delay values are restricted to an interval, which can be specified using the `-min/--mindelay` and `-max/--maxdelay` options. Here is example of how to use these options.

    java -jar simulator.jar -t topology.topo -d 0 -c 10 -min 100 -max 1000

This command executes 10 simulation runs, advertising destination with ID `0` on the topology specified in file `topology.topo`. All routing messages will be subjected to delays with values between `100` and `1000` (both inclusive).

By default, that is if the `-min` and `-max` options are not used, then the minimum and maximum delays are set to 1, meaning that all messages will have a delay of 1.

### How to repeat the exact same simulation?

The randomness of the delays imposed to the routing messages makes harder to reproduce a simulation run. But, not that hard. The generator that generates these delays accepts a seed value that determines the sequence of delays that it generates. Therefore, to reproduce a simulation we need to force the simulator to use the same seed value as the one used on that simulation. That can be accomplished with the `-seed` option as follows.

    java -jar simulator.jar -t topology.topo -d 0 -min 100 -max 1000 -seed 129312379172

This command will execute a single simulation run using `129312379172` as the seed to generate the message delays.

*Warning: make sure that input parameters are exactly the same as well!*

Since the only thing we need is the initial seed used in a previous simulation to reproduce it, we need to know where to get that value. It can be obtained from the `.basic.csv` file, output by the simulator.

| Note |
|:---|
| By default, the first seed used to generate the message delays during the first simulation run is obtained from the current time (real time). The seeds used for the next simulation runs are obtained from the last value generated in the previous simulation. |

### How to set the simulation threshold?

Each run simulates the advertisement of a single destination over a network. It terminates once all nodes have reached a stable state, that is none of them has any new route to announce, and no route is currently in transit. However, the Border Gateway Protocol (BGP) does not give any guarantee of termination. Thus, the simulation itself may never terminate. To prevent this from happening, a threshold value is established for each simulation run. This threshold value corresponds to the maximum simulation time. If the simulation reaches this threshold, it is immediately interrupted and labeled as *not terminated*.

The threshold value is set to 1000000 by default. This value can be changed with the `-th/--threshold` option as follows.

    java -jar simulator.jar -t topology.topo -d 0 -c 10 -th 15000000

This command will execute 10 simulation runs. Each one will use the 15000000 as the threshold value.

*Suggestion: consider adjusting the threshold value accordingly to the minimum and maximum delay values.*

### How to obtain more information about each node?

By default, the simulator outputs global information about the protocol. Option `-rn/--reportnodes` can be used to tell the simulator to output detailed information about each node. Here is an example of its usage.

    java -jar simulator.jar -t topology.topo -d 0 -c 10 --reportnodes

With this option set, the simulator will output a new file with extension `.nodes.csv`. This file is also a CSV file containing a table, which holds the following information for each node and each simulation:

- **Local Preference** - LOCAL-PREF of route elected by the node;
- **Next-hop** - ID of neighbor elected by the node;
- **Path Length** - length of the AS-PATH of route elected by the node;
- **Termination Time** - time at which the node sent its last route.

The first two columns of the table include the simulation number and the ID of the node. The simulation number corresponds to the same number included in the `.basic.csv`, so the two tables can be related. In database terms, this would mean that the two tables could be joined using their simulation numbers.

## Troubleshooting

1. When trying to run the simulator, I get the following error message.

        Error: Unable to access jarfile

   This error is shown when the path to the JAR file is not correct. Make sure the specified path points to the correct JAR file.

## Authors

- David Fialho, fialho.david@protonmail.com
