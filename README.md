# LinkedPipes ETL
[![Build Status](https://travis-ci.com/linkedpipes/etl.svg?branch=develop)](https://travis-ci.com/linkedpipes/etl)

LinkedPipes ETL is an RDF based, lightweight ETL tool.
- [REST API](https://github.com/linkedpipes/etl/wiki) based set of components for easy integration
- [Library of components](https://etl.linkedpipes.com/components) to get you started faster
- [Sharing of configuration](https://etl.linkedpipes.com/templates/) among individual pipelines using templates
- RDF configuration of transformation pipelines

## Requirements
- Linux, Windows, iOS
- [Docker], [Docker Compose]

### For building locally
- [Java] 11 or 16
- [Git]
- [Maven], 3.2.5 or newer
- [Node.js] & npm

## Installation and startup
You can run LP-ETL in Docker, or build it from the source.

### Docker
To start LP-ETL ```master``` branch on ```http://localhost:8080```, you can use a one-liner:
```
curl https://raw.githubusercontent.com/linkedpipes/etl/master/docker-compose.yml | docker-compose -f - up
```
Note that on Windows, there is an [issue with buildkit](https://github.com/moby/buildkit/issues/1684).
See the [temporary workaround](https://github.com/linkedpipes/etl/issues/851#issuecomment-814058925).

When running this on Windows, you might get a build error. There is a [workaround](https://github.com/linkedpipes/etl/issues/851) for that.

Alternatively, you can build docker images from GitHub sources using a one-liner:
```
curl https://raw.githubusercontent.com/linkedpipes/etl/master/docker-compose-github.yml | docker-compose -f - up
```

You may need to run the commands as ```sudo``` or be in the ```docker``` group.

#### Configuration
Each component (executor, executor-monitor, storage, frontend) has separate ```Dockerfile```.

Environment variables:
 * ```LP_ETL_BUILD_BRANCH``` - The ```Dockerfiles``` are designed to run build from the github repository, the branch is set using this property, default is ```master```.
 * ```LP_ETL_BUILD_JAVA_TEST``` - Set to empty to allow to run Java tests, this will slow down the build.
 * ```LP_ETL_DOMAIN``` - The URL of the instance, this is used instead of the ```domain.uri``` from the configuration.
 * ```LP_ETL_FTP``` - The URL of the FTP server, this is used instead of the ```executor-monitor.ftp.uri``` from the configuration. 
 
For [Docker Compose], there are additional environment variables:
 * ```LP_ETL_PORT``` - Specify port mapping for frontend, this is where you can connect to your instance.
This does NOT have to be the same as port in ```LP_ETL_DOMAIN``` in case of reverse-proxying.

For example to run LP-ETL from ```develop``` branch on ```http://localhost:9080``` use can use following command:
```
curl https://raw.githubusercontent.com/linkedpipes/etl/develop/docker-compose-github.yml | LP_ETL_PORT=9080 LP_ETL_DOMAIN=http://localhost:9080 LP_ETL_BUILD_BRANCH=develop docker-compose -f - up
```

```docker-compose``` utilizes several volumes that can be used to access/provide data.
See ```docker-compose.yml``` comments for examples and configuration.
You may want to create your own ```docker-compose.yml``` for custom configuration.

### From source on Linux

#### Installation

```sh
$ git clone https://github.com/linkedpipes/etl.git
$ cd etl
$ mvn install
```

#### Configuration
The configuration file ```deploy/configuration.properties``` can be edited, mainly changing paths to working, storage, log and library directories. 

#### Startup

```sh
$ cd deploy
$ ./executor.sh >> executor.log &
$ ./executor-monitor.sh >> executor-monitor.log &
$ ./storage.sh >> storage.log &
$ ./frontend.sh >> frontend.log &
```

#### Running LP-ETL as a systemd service
See example service files in the ```deploy/systemd``` folder.

### From source on Windows
Note that it is also possible to use [Bash on Ubuntu on Windows] or [Cygwin] and proceed as with Linux.

#### Installation
```sh
git clone https://github.com/linkedpipes/etl.git
cd etl
mvn install
```
#### Configuration
The configuration file ```deploy/configuration.properties``` can be edited, mainly changing paths to working, storage, log and library directories. 

#### Startup
In the ```deploy``` folder, run
 * ```executor.bat```
 * ```executor-monitor.bat```
 * ```storage.bat```
 * ```frontend.bat```

## Plugins - Components
The components live in the ```jars``` directory.
Detailed description of how to create your own is coming soon, in the meantime, you can copy an existing component and change it.
 
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
