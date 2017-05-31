# LinkedPipes ETL

LinkedPipes ETL is an RDF based, lightweight ETL tool.
- [REST API](https://github.com/linkedpipes/etl/wiki) based set of components for easy integration
- [Library of components](http://etl.linkedpipes.com/components) to get you started faster
- [Sharing of configuration](http://etl.linkedpipes.com/templates/) among individual pipelines using templates
- RDF configuration of transformation pipelines

## Requirements
- Linux, Windows, iOS
- [Java 8], update 101 or newer
- [Git]
- [Maven], 3.2.5 or newer
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
Now edit the configuration file, mainly adding paths to working, storage, log and library directories. Especially:
* ```executor.execution.working_directory```
* ```executor.log.directory```
* ```executor.osgi.lib.directory```
* ```executor-monitor.log.directory```
* ```storage.jars.directory```
* ```storage.directory```
* ```storage.log.directory```

For an example see the ```configuration.properties.example``` file.

## Running LinkedPipes ETL
To run LP-ETL, you need to run the three components it consists of. For debugging purposes, it is useful to store the console logs.

### Linux
```sh
$ cd deploy
$ ./executor.sh >> executor.log &
$ ./executor-monitor.sh >> executor-monitor.log &
$ ./storage.sh >> storage.log &
$ ./frontend.sh >> frontend.log &
```

### Windows
We recommend using [Cygwin] and proceeding as with Linux. Otherwise, in the ```deploy``` folder, run
 * ```executor.bat```
 * ```executor-monitor.bat```
 * ```sotrage.bat```
 * ```frontend.bat```

Unless configured otherwise, LinkedPipes ETL should now run on ```http://localhost:8080```.
## Plugins - Components
There are components in the ```jars``` directory. Detailed description of how to create your own coming soon.

## Known issues
 * On some Linux systems, Node.js may be run by ```nodejs``` instead of ```node```. In that case, you need to rewrite this in the ```deploy/frontend.sh``` script.
 * If you are using Oracle Java 8, accessing HTTPS based URLs and getting ```SSLHandshakeException : Received fatal alert: handshake_failure``` when the same URL works from, e.g. Chrome, try installing the [Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files for JDK/JRE 8](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html)
 
## Update notes
> Update note 3: When upgrading from develop prior to 2017-02-14, you need to delete ```{deploy}/jars``` and ```{deploy}/osgi```. 

> Update note 2: When upgrading from master prior to 2016-11-04, you need to move your pipelines folder from e.g. ```/data/lp/etl/pipelines``` to ```/data/lp/etl/storage/pipelines```, update the configuration.properites file and possibly the update/restart scripts as there is a new component, ```storage```.

> Update note: When upgrading from master prior to 2016-04-07, you need to delete your old execution data (e.g. in /data/lp/etl/working/data)
 
## Update script
Since we are still in the rapid development phase, we update our instance often. This is an update script that we use and you can reuse if you wish. The script sets path to Java 8, kills running components (yeah, it is dirty), the repo is cloned in ```/opt/lp/etl``` and we store the console logs in ```/data/lp/etl```
```sh
#!/bin/bash
echo Killing Executor
kill `ps ax | grep /executor.jar | grep -v grep | awk '{print $1}'`
echo Killing Executor-monitor
kill `ps ax | grep /executor-monitor.jar | grep -v grep | awk '{print $1}'`
echo Killing Frontend
kill `ps ax | grep node | grep -v grep | awk '{print $1}'`
echo Killing Storage
kill `ps ax | grep /storage.jar | grep -v grep | awk '{print $1}'`
cd /opt/lp/etl
echo Git Pull
git pull
echo Mvn install
mvn clean install
cd deploy
echo Running executor
./executor.sh >> /data/lp/etl/executor.log &
echo Running executor-monitor
./executor-monitor.sh >> /data/lp/etl/executor-monitor.log &
echo Running storage
./storage.sh >> /data/lp/etl/storage.log &
echo Running frontend
./frontend.sh >> /data/lp/etl/frontend.log &
echo Disowning
disown
```

[Java 8]: <http://www.oracle.com/technetwork/java/javase/downloads/index.html>
[Git]: <https://git-scm.com/>
[Maven]: <https://maven.apache.org/>
[Node.js]: <https://nodejs.org>
[Cygwin]: <https://www.cygwin.com/>
