((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "@client/app-service/vocabulary",
      "@client/app-service/jsonld/jsonld",
      "./execution-mapping"
    ], definition);
  }
})((_vocabulary, jsonld, MAPPING_STATUS) => {
  "use strict";

  const LP = _vocabulary.LP;

  function getComponent(model, iri) {
    if (model.components[iri] === undefined) {
      model.components[iri] = createComponent(iri);
    }
    return model.components[iri];
  }

  function createComponent(iri) {
    return {
      "iri": iri
    };
  }

  function onExecution(model, resource) {
    model.execution.iri = resource["@id"];
    model.pipeline.iri = jsonld.r.getIRI(resource, LP.HAS_PIPELINE);
    const status = jsonld.r.getIRI(resource, LP.HAS_STATUS);
    model.execution.status.iri = status;
    model.execution.status.running = isExecutionRunning(status);
    model.execution.deleteWorkingData =
      jsonld.r.getBoolean(resource, LP.HAS_DELETE_WORKING_DATA);
  }

  function isExecutionRunning(status) {
    switch (status) {
      case LP.EXEC_FINISHED:
      case LP.EXEC_FAILED:
      case LP.EXEC_CANCELLED:
        return false;
      default:
        return true;
    }
  }

  function onComponent(model, resource) {
    const iri = jsonld.r.getId(resource);
    const component = getComponent(model, iri);
    component.status = jsonld.r.getIRI(resource, LP.HAS_STATUS);
    component.order = jsonld.r.getInteger(resource, LP.HAS_EXEC_ORDER);
    component.dataUnits = jsonld.r.getIRIs(resource, LP.HAS_DU);
    component.mapping = convertMapping(component.status);
    const execution = jsonld.r.getIRI(resource, LP.HAS_EXECUTION);
    if (execution) {
      component.execution = execution;
    }
  }

  function convertMapping(status) {
    switch (status) {
      case LP.EXEC_FINISHED:
      case LP.EXEC_MAPPED:
        return MAPPING_STATUS.FINISHED_MAPPED;
      case LP.EXEC_FAILED:
        return MAPPING_STATUS.FAILED;
      case LP.EXEC_CANCELLED:
        return MAPPING_STATUS.UNFINISHED_MAPPED;
      default:
        return MAPPING_STATUS.UNFINISHED;
    }
  }

  function onDataUnit(model, resource) {
    const iri = resource["@id"];
    model.dataUnits[iri] = {
      "iri": iri,
      "binding": jsonld.r.getPlainString(resource, LP.HAS_DU_BINDING),
      "debug": jsonld.r.getPlainString(resource, LP.HAS_DEBUG)
    };
  }

  function loadModelFromJsonLd(model, data, graphIri) {
    console.time("Loading pipeline execution");
    const graph = jsonld.q.getGraph(data, graphIri);
    jsonld.t.iterateResources(graph, (resource) => {
      const types = jsonld.r.getTypes(resource);
      for (let index in types) {
        const type = types[index];
        const action = loadActions[type];
        if (action !== undefined) {
          action(model, resource);
        }
      }
    });
    console.timeEnd("Loading pipeline execution");
  }

  //
  //
  //

  const loadActions = {};

  (function initialize() {
    loadActions[LP.EXECUTION] = onExecution;
    loadActions[LP.COMPONENT] = onComponent;
    loadActions[LP.DATA_UNIT] = onDataUnit;
  })();

  return {
    "loadModelFromJsonLd": loadModelFromJsonLd
  };

});
