# REST API
The LinkedPipes ELT consists of several components:
* Executor
* Executor-Monitor
* Storage
* Frontend

If you are interested only in a pipeline execution you can take a look at [example using curl](/linkedpipes/etl/wiki/Pipeline-execution-with-curl)

### Executor
Executor is responsible for execution of a single pipeline, only one pipeline can be executed by executor at a time.
Executor loads pipeline definition from local machine and executes it.
For the duration of the pipeline execution a status monitor is accessible.

### Executor-Monitor
Executor-Monitor can be seen as an instance manager.
It accepts pipelines for executions and execute them using Executor.
It is also responsible for providing access to logs, debug data and pipeline status.

### Storage
Storage manages pipelines and components.
It is also responsible for converting pipelines to form in which they can be executed.

### Frontend
Host LP ETL web-client and serve as a API gate.
For now also takes responsibility for managing process before the pipeline execution, ie. retrieve pipeline definition, unpack pipeline and submit it for executor to Executor-Monitor. 
