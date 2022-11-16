# Documentation

## Components
The LinkedPipes ELT consists of several components.

### Executor
Executor is responsible for execution of a single pipeline.
Only one pipeline can be executed by executor at a time.
Executor loads pipeline definition from local machine and executes it.
For the duration of the pipeline execution a status HTTP endpoint can be accessible.

### Executor-Monitor
Executor-Monitor can be seen as an instance manager.
It accepts pipelines for executions and execute them using _Executor_.
It is also responsible for providing access to logs, debug data, and pipeline status.

### Storage
Storage manages pipelines and components.

### Frontend
Host web-client and serve as API gateway.
It also implements composite operation like executing a given pipeline.

## Opem API
There is a partial documentation of [public API](./openapi-public.yaml). 
You can deploy your own Swagger editor with Docker using following command:
```bash
docker run --rm -p 8095:8080 swaggerapi/swagger-editor
```

## Actions
The documentation also cover some how-to:
* [Change Domain](./how-to/change-domain.md)
* [Create a Plugin](./how-to/create-plugin.md)
