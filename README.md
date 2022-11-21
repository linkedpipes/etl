# LinkedPipes ETL
[![Travis Status](https://travis-ci.com/linkedpipes/etl.svg?branch=develop)](https://travis-ci.com/linkedpipes/etl)

LinkedPipes ETL is an RDF based, lightweight ETL tool.
- [Library of components](https://etl.linkedpipes.com/components) to get you started faster
- [Sharing of configuration](https://etl.linkedpipes.com/templates/) among individual pipelines using templates
- RDF configuration of transformation pipelines

## Requirements
- Linux, Windows, iOS
- [Docker], [Docker Compose]

### For building locally
- [Java] 17, 18
- [Git]
- Optionally [Maven]
- [Node.js] 18 & npm

## Installation and startup
You can run LP-ETL in Docker, or build it from the source.

### Docker
To start LP-ETL you can use:
```
git clone https://github.com/linkedpipes/etl.git
cd etl
docker-compose up
```
This would use pre-build images stored at [DockerHub].
The images are build from the main branch.

Alternatively you can use one liner.
For example to run LP-ETL from ```develop``` branch on ```http://localhost:9080``` use can use following command:
```
curl https://raw.githubusercontent.com/linkedpipes/etl/develop/docker-compose-github.yml | LP_ETL_PORT=9080 LP_VERSION=develop docker-compose -f - up
```

You may need to run the ```docker-compose``` command as ```sudo``` or be in the ```docker``` group.

#### Building Docker images
You can build LP-ETL images your self.
Note that on Windows, there is an [issue with buildkit](https://github.com/moby/buildkit/issues/1684).
See the [temporary workaround](https://github.com/linkedpipes/etl/issues/851#issuecomment-814058925).

#### Configuration
Environment variables:
- ```LP_VERSION``` - default value ```main```, determine the version of Docker images.
- ```LP_ETL_DOMAIN``` - The URL of the instance, this is used instead of the ```domain.uri``` from the configuration. 
- ```LP_ETL_PORT``` - Specify port mapping for frontend, this is where you can connect to your instance.
  This does NOT have to be the same as port in ```LP_ETL_DOMAIN``` in case of reverse-proxying.

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
- ```executor.bat```
- ```executor-monitor.bat```
- ```storage.bat```
- ```frontend.bat```

## Data import
You can copy pipelines and templates data from one instance to another directly.

Assume that you have copy of a data directory ```./data-source``` with ```pipelines``` and ```templates``` subdirectories. 
You can obtain the directory from any running instance, you can even merge content of multiple of those directories together.
In the next step you would like to import the data into a new instance. 
You can just copy the files to respective directories under ```./data-target```.
Keep in mind that this would preserve the IRIs.

Should you need to change the IRIs, you should employ import and export functionality available in the frontend.

## Plugins - Components
The components live in the ```jars``` directory.
If you need to create your own component, you can copy an existing component and change it.
 
## Update notes
> Update note 5: 2019-09-03 breaking changes in the configuration file. Remove ```/api/v1``` from the ```executor-monitor.webserver.uri```, so it looks like: ```executor-monitor.webserver.uri = http://localhost:8081```. You can also remove ```executor.execution.uriPrefix``` as the value is derived from ```domain.uri```.

> Update note 4: 2019-07-03 we changed the way frontend is run. If you do not use our script to run it, you need to update yours. 

> Update note 3: When upgrading from develop prior to 2017-02-14, you need to delete ```{deploy}/jars``` and ```{deploy}/osgi```. 

> Update note 2: When upgrading from master prior to 2016-11-04, you need to move your pipelines folder from e.g., ```/data/lp/etl/pipelines``` to ```/data/lp/etl/storage/pipelines```, update the configuration.properites file and possibly the update/restart scripts as there is a new component, ```storage```.

> Update note 1: When upgrading from master prior to 2016-04-07, you need to delete your old execution data (e.g., in /data/lp/etl/working/data)

[Java]: <http://www.oracle.com/technetwork/java/javase/downloads/index.html>
[Git]: <https://git-scm.com/>
[Maven]: <https://maven.apache.org/>
[Node.js]: <https://nodejs.org>
[Cygwin]: <https://www.cygwin.com/>
[Bash on Ubuntu on Windows]: <https://msdn.microsoft.com/en-us/commandline/wsl/about>
[Docker]: <https://www.docker.com/>
[Docker Compose]: <https://docs.docker.com/compose/>
[DockerHub]: <https://hub.docker.com/>
