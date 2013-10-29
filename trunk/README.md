# Jalen

Jalen is a Java agent for monitoring energy at the code level.
It uses power information provided by [PowerAPI](http://www.powerapi.org/ "PowerAPI") (v. 1.6+), in order to estimate the energy consumption of software code.
In particular, Jalen provides energy information at finer grain, i.e., at the level of Java methods.
Concretely, Jalen is implemented as a Java agent that hooks to the Java Virtual Machine during its start, and monitors and collects energy related information of the executed Java application.
Jalen uses statistical sampling in order to estimate resources usage, such as the CPU utilization, disk and network access.
It produces energy files where the energy consumption of each method, and by each monitored hardware resource is provided.
Jalen can also monitor selected methdods, classes and packages, therefore ignoring other methods and aggregating children methods.

## Documentation
* [Getting started](#getting-started)
* [Future works](#future-works)
* [License](#license)

<h2 id="getting-started">Getting started</h2>

Jalen is fully written in Java (v. 7+) and its project is fully managed by [Maven](http://maven.apache.org "Maven") (v. 3).

### How to install it

In order to install and use Jalen, download the source code by cloning it via your Git client:

```bash
git clone git://github.com/adelnoureddine/jalen.git
```

Jalen uses [Maven](http://maven.apache.org "Maven") to manage the project, therefore your need to compile the Java source file and generate the Jalen .jar archive by typying the following commands:

```bash
cd $JALEN_DIR
mvn clean package -Pstandalone
```

A `build.sh` file is also provided to automate the compilation and generation of Jalen by using:
```bash
cd $JALEN_DIR
sh buil.sh
```

### How to use it

You simply need to inject the jar file for Jalen as a Java agent during the start of your program:

```bash
java -Dconfig.file=./configuration/application.conf -javaagent:$JALEN_AGENT.jar -jar $YOUR_PROGRAM.jar
```

For example, to use Jalen version 1.0:

```bash
java -Dconfig.file=./configuration/application.conf -javaagent:jalen-1.0-SNAPSHO-jar-with-dependencies.jar -jar $YOUR_PROGRAM.jar
```

### How to configure it

Configuring Jalen is achieved by modifying the `config.properties` file.


<h2 id="future-works">Future works</h2>

We are working on adding new _energy models_ for estimating the energy consumption of software by other hardware resources. If you are interested in joining, feel free to contact us via our [GitHub](https://github.com/adelnoureddine/jalen "GitHub") webpage or email us at adel.noureddine@inria.fr!

<h2 id="license">License</h2>

Copyright (C) 2013, Adel Noureddine.

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
