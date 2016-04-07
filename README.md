# LinkedPipes ETL

> Upgrade note: When upgrading from master prior to 2016-04-07 to master after 2016-04-07, you need to delete your old execution data (e.g. in /data/lp/etl/working/data)

LinkedPipes ETL is an RDF based, lightweight ETL tool.
- REST API based set of components for easy integration
- Library of Data Processing Units (DPUs) to get you started faster
- RDF configuration of transformation pipelines

## Requirements
- Linux or Windows
- [Java 8]
- [Git]
- [Maven]
- [Node.js] & npm

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
```sh
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
 * On Linux systems, the .sh scripts are not executable after ```mvn install```. This can be solved easily by setting the proper mode, ```chmod +x *.sh```. https://github.com/linkedpipes/etl/issues/12

## Update script
Since we are still in the rapid development phase, we update our instance often. This is an update script that we use and you can reuse if you wish. The script sets path to Java 8, kills running components (yeah, it is dirty), the repo is cloned in ```/opt/etl``` and we store the console logs in ```/data/lp/```
```sh
#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-8-oracle
echo Killing Executor
kill `ps ax | grep /executor.jar | grep -v grep | awk '{print $1}'`
echo Killing Executor-monitor
kill `ps ax | grep /executor-monitor.jar | grep -v grep | awk '{print $1}'`
echo Killing Executor-view
kill `ps ax | grep node | grep -v grep | awk '{print $1}'`
cd /opt/etl
echo Git Pull
git pull
echo Mvn install
mvn clean install
cd deploy
echo Running executor
./executor.sh >> /data/lp/executor.log &
echo Running executor-monitor
./executor-monitor.sh >> /data/lp/executor-monitor.log &
echo Runninch executor-view
./frontend.sh >> /data/lp/frontend.log &
echo Disowning
disown
```

[Java 8]: <http://www.oracle.com/technetwork/java/javase/downloads/index.html>
[Git]: <https://git-scm.com/>
[Maven]: <https://maven.apache.org/>
[Node.js]: <https://nodejs.org>
[Cygwin]: <https://www.cygwin.com/>
