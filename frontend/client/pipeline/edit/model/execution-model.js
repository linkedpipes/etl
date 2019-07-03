((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "./execution-model-loader",
      "./execution-mapping"
    ], definition);
  }
})((loader, MAPPING_STATUS) => {

  const service = {};

  service.createFromJsonLd = (jsonld, graph) => {
    const data = {
      "pipeline": {},
      "components": {},
      "dataUnits": {},
      "metadata": {
        "hasWorkingData": void 0
      },
      "execution": {
        "status": {},
        "iri": void 0
      }
    };
    loader.loadModelFromJsonLd(data, jsonld, graph);
    return data;
  };

  service.updateFromJsonLd = (model, jsonld) => {
    loader.loadModelFromJsonLd(model, jsonld, model.execution.iri);
  };

  service.getComponent = (model, iri) => {
    return model.components[iri];
  };

  service.getIri = (model) => {
    return model.execution.iri;
  };

  service.getDataUnit = (model, component, binding) => {
    for (let index in component.dataUnits) {
      const dataUnit = model.dataUnits[component.dataUnits[index]];
      if (dataUnit !== undefined && dataUnit.binding === binding) {
        return dataUnit;
      }
    }
    return undefined;
  };

  service.hasExecutionWorkingData = (model) => {
    return model.execution.deleteWorkingData !== true;
  };

  service.isExecutionFinished = (model) => {
    if (model.execution.status.running === undefined) {
      return false;
    }
    return !model.execution.status.running;
  };

  service.getExecutionStatus = (model) => {
    return model.execution.status.iri;
  };

  /**
   * True if mapping can be changed.
   */
  service.canChangeMapping = (model, component) => {
    switch (component.mapping) {
      case MAPPING_STATUS.FINISHED_MAPPED:
      case MAPPING_STATUS.FINISHED:
        return true;
      default:
        return false;
    }
  };

  service.isChanged = (model, component) => {
    return component.mapping === MAPPING_STATUS.CHANGED;
  };

  service.onChange = (model, component) => {
    switch (component.mapping) {
      case MAPPING_STATUS.FINISHED_MAPPED:
      case MAPPING_STATUS.FINISHED:
        component.mapping = MAPPING_STATUS.CHANGED;
        break;
    }
  };

  /**
   * If component has not changed, mapping is available and is
   * enabled, then it's used in the execution.
   */
  service.isUsedForExecution = (model, component) => {
    return component.mapping === MAPPING_STATUS.FINISHED_MAPPED;
  };

  /**
   * If true mapping based on the status is used otherwise
   * a "disable" mapping should be used.
   */
  service.isMappingEnabled = (model, component) => {
    switch (component.mapping) {
      case MAPPING_STATUS.FINISHED_MAPPED:
      case MAPPING_STATUS.FAILED:
        return true;
      default:
        return false;
    }
  };

  service.getComponentStatus = (model, component) => {
    return component.status;
  };

  service.enableMapping = (model, component) => {
    switch (component.mapping) {
      case MAPPING_STATUS.FINISHED:
        component.mapping = MAPPING_STATUS.FINISHED_MAPPED;
        break
    }
  };

  service.disableMapping = (model, component) => {
    switch (component.mapping) {
      case MAPPING_STATUS.FINISHED_MAPPED:
        component.mapping = MAPPING_STATUS.FINISHED;
        break
    }
  };

  /**
   * Model that can be used if no execution is available.
   */
  service.emptyModel = () => {
    return {
      "execution": {
        "iri": undefined,
        "status": {}
      },
      "components": {},
      "dataUnits": {}
    }
  };

  return service;
});