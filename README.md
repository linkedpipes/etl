# LinkedPipes ETL

LinkedPipes ETL is an RDF based, lightweight ETL tool.
- REST API based set of components for easy integration
- Library of Data Processing Units (DPUs) to get you started faster
- RDF configuration of transformation pipelines

## Requirements
- Linux or Windows
- [Java 8]
- [Git]
- [Maven]
- [Node.js]

## Installation
So far, you need to compile LP-ETL on your own:

### Linux
```sh
$ git clone https://github.com/linkedpipes/etl.git
$ cd etl
$ mvn install
$ cp configuration.properties.sample deploy/configuration.properties
$ vi deploy/configuration.properties
```
### Windows
We recommend using [Cygwin] and proceeding as with Linux.

### Configuration
Now edit the configuration file, mainly adding paths to working, storage, log and library directories.

## Running LinkedPipes ETL
To run LP-ETL, you need to run the three components it consists of. For debugging purposes, it is useful to store the console logs.

### Linux
```
$ cd deploy
$ ./executor.sh >> executor.log &
$ ./executor-monitor.sh >> executor-monitor.log &
$ ./frontend.sh >> frontend.log &
```

### Windows
We recommend using [Cygwin] and proceeding as with Linux. Otherwise, in the ```deploy``` folder, run
 * ```executor.bat```
 * ```executor-monitor.bat```
 * ```frontend.bat```

Unless configured otherwise, LinkedPipes ETL should now run on ```http://localhost:8080```.
## Plugins - DPUs
There are data processing units (DPUs) in the plugins directory. Detailed description of how to create your own coming soon.

## Known issues
 * On some Linux systems, Node.js may be run by ```nodejs``` instead of ```node```. In that case, you need to rewrite this in the ```deploy/frontend.sh``` script.

[Java 8]: <http://www.oracle.com/technetwork/java/javase/downloads/index.html>
[Git]: <https://git-scm.com/>
[Maven]: <https://maven.apache.org/>
[Node.js]: <https://nodejs.org>
[Cygwin]: <https://www.cygwin.com/>
