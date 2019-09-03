# LinkedPipes ETL
[![Build Status](https://travis-ci.com/linkedpipes/etl.svg?branch=develop)](https://travis-ci.com/linkedpipes/etl)

LinkedPipes ETL is an RDF based, lightweight ETL tool.
- [REST API](https://github.com/linkedpipes/etl/wiki) based set of components for easy integration
- [Library of components](https://etl.linkedpipes.com/components) to get you started faster
- [Sharing of configuration](https://etl.linkedpipes.com/templates/) among individual pipelines using templates
- RDF configuration of transformation pipelines

## Requirements
- Linux, Windows, iOS
- [Java] 11 or 12
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
```
### Windows
We recommend using [Bash on Ubuntu on Windows] or [Cygwin] and proceeding as with Linux.
Nevertheless, it is possible to build and use LP-ETL with pure Windows-based versions of tools.

## Running LinkedPipes ETL
To run LP-ETL, you need to run the four components it consists of. For debugging purposes, it is useful to store the console logs.

### Linux
```sh
$ cd deploy
$ ./executor.sh >> executor.log &
$ ./executor-monitor.sh >> executor-monitor.log &
$ ./storage.sh >> storage.log &
$ ./frontend.sh >> frontend.log &
```

### Windows
We recommend using [Bash on Ubuntu on Windows] or [Cygwin] and proceeding as with Linux. 
Otherwise, in the ```deploy``` folder, run
 * ```executor.bat```
 * ```executor-monitor.bat```
 * ```storage.bat```
 * ```frontend.bat```

Unless configured otherwise, LinkedPipes ETL should now run on ```http://localhost:8080```.

## Docker
LP-ETL is ready to be run using [Docker]. Each component (executor, executor-monitor, storage, frontend) has separate ```Dockerfile```.

Environment variables:
 * ```LP_ETL_BUILD_BRANCH``` - The ```Dockerfiles``` are designed to run build from the github repository, the branch is set using this property, default is ```master```.
 * ```LP_ETL_BUILD_JAVA_TEST``` - Set to empty to allow to run Java tests, this will slow down the build.
 * ```LP_ETL_DOMAIN``` - The URL of the instance, this is used instead of the ```domain.uri``` from the configuration.
 * ```LP_ETL_FTP``` - The URL of the FTP server, this is used instead of the ```executor-monitor.ftp.uri``` from the configuration. 
 
As there are multiple components we decide to employ [Docker Compose] to make running LP-ETL easier. There are additional environment variables:
 * ```LP_ETL_PORT``` - Specify port mapping for frontend, this is where you can connect to your instance. This does NOT have to be the same as port in ```LP_ETL_DOMAIN```. 

The ```docker-compose``` utilizes several volumes that can be used to access/provide data (see ```docker-compose.yml``` comments for example) and configuration. 

You can run the LP-ETL using ```docker-compose``` with a one-liner:
```
curl https://raw.githubusercontent.com/linkedpipes/etl/master/docker-compose.yml | LP_ETL_PORT=9080 LP_ETL_DOMAIN=http://localhost:9080 docker-compose -f - up
```
you may need to run this as ```sudo``` based on your system configuration. This example start LP-ETL from ```master``` branch, and made it available on port ```9080``` with domain URL ```http://localhost:9080```. 
NOTE: To change the branch you need to use ```LP_ETL_BUILD_BRANCH``` to change code,  change the URL to use different version of ```docker-compose``` file.
```
curl https://raw.githubusercontent.com/linkedpipes/etl/develop/docker-compose.yml | LP_ETL_PORT=9080 LP_ETL_DOMAIN=http://localhost:9080 LP_ETL_BUILD_BRANCH=develop docker-compose -f - up
```

## Plugins - Components
There are components in the ```jars``` directory. Detailed description of how to create your own coming soon.

## Configuration
The configuration file in the `deploy` directory can be edited, mainly changing paths to working, storage, log and library directories. 

## Update script
Since we are still in the rapid development phase, we update our instance often. This is an update script that we use and you can reuse it if you wish. The script kills the running components (yeah, it is dirty), the repo is cloned in ```/opt/lp/etl``` and we store the console logs in ```/data/lp/etl```.
We recommend running the 4 components as individual services though.
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
 
## Update notes
> Update note 5: 2019-09-03 breaking changes in the configuration file. Remove ```/api/v1``` from the ```executor-monitor.webserver.uri```, so it loolks like: ```executor-monitor.webserver.uri = http://localhost:8081```. You can also remove ```executor.execution.uriPrefix``` as the value is derived from ```domain.uri```.

> Update note 4: 2019-07-03 we changed the way frontend is run. If you do not use our script to run it, you need to update yours. 

> Update note 3: When upgrading from develop prior to 2017-02-14, you need to delete ```{deploy}/jars``` and ```{deploy}/osgi```. 

> Update note 2: When upgrading from master prior to 2016-11-04, you need to move your pipelines folder from e.g., ```/data/lp/etl/pipelines``` to ```/data/lp/etl/storage/pipelines```, update the configuration.properites file and possibly the update/restart scripts as there is a new component, ```storage```.

> Update note: When upgrading from master prior to 2016-04-07, you need to delete your old execution data (e.g., in /data/lp/etl/working/data)

[Java]: <http://www.oracle.com/technetwork/java/javase/downloads/index.html>
[Git]: <https://git-scm.com/>
[Maven]: <https://maven.apache.org/>
[Node.js]: <https://nodejs.org>
[Cygwin]: <https://www.cygwin.com/>
[Bash on Ubuntu on Windows]: <https://msdn.microsoft.com/en-us/commandline/wsl/about>
[Docker]: <https://www.docker.com/>
[Docker Compose]: <https://docs.docker.com/compose/>