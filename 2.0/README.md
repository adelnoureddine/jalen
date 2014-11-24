# Jalen 2

Jalen is a Java agent for monitoring energy at the code level.
In particular, Jalen provides energy information at finer grain, i.e., at the level of Java methods.
Concretely, Jalen is implemented as a Java agent that hooks to the Java Virtual Machine during its start, and monitors and collects energy related information of the executed Java application.
Jalen uses statistical sampling in order to estimate resources usage, such as the CPU utilization, disk and network access.
It produces energy files where the energy consumption of each method, and by each monitored hardware resource is provided.
Jalen can also monitor selected methdods, classes and packages, therefore ignoring other methods and aggregating children methods.

## Documentation
* [Getting started](#getting-started)
* [Future works](#future-works)
* [License](#license)

## Getting started

Jalen is fully written in Java (v. 7+) and its project is fully managed by [Maven](http://maven.apache.org "Maven") (v. 3).

### How to use it

You simply need to inject the jar file for Jalen as a Java agent during the start of your program:

```bash
java -javaagent:$JALEN_AGENT.jar -jar $YOUR_PROGRAM.jar
```

### How to build it

In order to build and use Jalen, download the source code by cloning it via your Git client:

```bash
git clone git://github.com/adelnoureddine/jalen.git
```

Jalen uses [Maven](http://maven.apache.org "Maven") to manage the project, therefore your need to compile the Java source file and generate the Jalen .jar archive by typying the following commands:

```bash
cd $JALEN_DIR
mvn clean install
```

A `build.sh` file is also provided to automate the compilation and generation of Jalen by using:
```bash
cd $JALEN_DIR
sh build.sh
```

### How to configure it

Configuring Jalen is achieved by modifying the `config.properties` file.

### Generated files

At the end of the execution of the Java application, Jalen will generate two files: `pid.csv` and `pid-filtered.csv`.
Both of these files have energy values, the difference is that the `pid.csv` file will have the energy consumption of all monitored methods, including the JDK's own methods.
`pid-filtered.csv`, on the other hand, will only have the energy consumption of methods prefixed in the configuration file.
This is useful to measure energy only for the application, or a library it calls, or even a subset of classes or methods.
The energy consumed by other methods (including JDK's ones) will be added to the prefixed method that called them.

The format of both files is as follows: `method-Name;CPU-Energy;Disk-Energy` where method-Name is the full name of the monitored method, CPU-Energy is the CPU energy consumed by the method and Disk-Energy is the disk energy of the method.

## Future works

We are working on adding new _energy models_ for estimating the energy consumption of software by other hardware resources, and also improving the source code and our software.

If you are interested in joining, feel free to contact us via our [GitHub](https://github.com/adelnoureddine/jalen "GitHub") webpage or email us at adel.noureddine@outlook.com!

## License

Copyright (c) 2014, Inria, University Lille 1.

Jalen is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Jalen is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Jalen. If not, see <http://www.gnu.org/licenses/>.