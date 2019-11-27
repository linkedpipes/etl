((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "jquery",
      "@client/app-service/jsonld/jsonld",
      "@client/app-service/vocabulary",
      "./model/pipeline-model",
      "./model/execution-model",
    ], definition);
  }
})((jQuery, jsonld, vocabulary, pplModel, execModel) => {

  const LP = vocabulary.LP;

  let $pipeline;

  let $execution;

  function getComponentFillColor(template, component) {
    let color = pplModel.component.getColor(component);
    if (color === undefined) {
      color = getEffectiveColor(template);
    }
    if (pplModel.component.isDisabled(component)) {
      color = "#f2f2f2";
    }
    return color;
  }

  function getEffectiveColor(template) {
    if (template.color) {
      return template.color;
    } else {
      const parent = template._parents[template._parents.length - 1];
      return getEffectiveColor(parent)
    }
  }

  const defaultRectStyle = {
    "stroke": "black",
    "stroke-width": 1
  };

  const statusToRectStyle = {
    [LP.EXEC_QUEUED]: {
      "stroke": "black",
      "stroke-width": 1
    },
    [LP.EXEC_INITIALIZING]: {
      "stroke": "blue",
      "stroke-width": 3
    },
    [LP.EXEC_RUNNING]: {
      "stroke": "blue",
      "stroke-width": 4
    },
    [LP.EXEC_FINISHED]: {
      "stroke": "#388E3C",
      "stroke-width": 4
    },
    [LP.EXEC_CANCELLED]: {
      "stroke": "#DED702",
      "stroke-width": 4
    },
    [LP.EXEC_MAPPED]: {
      "stroke": "#00796B",
      "stroke-width": 3
    },
    [LP.EXEC_FAILED]: {
      "stroke": "red",
      "stroke-width": 4
    },
    "disabled": {
      "stroke": "gray",
      "stroke-width": 2
    },
    "disabled-failed": {
      "stroke": "red",
      "stroke-width": 2
    },
    "disabled-mapped": {
      "stroke": "#DED702",
      "stroke-width": 2
    },
    "notAvailable": {
      "stroke": "black",
      "stroke-width": 1
    }
  };

  function getComponentRectStyle(component) {
    if (!$execution) {
      return defaultRectStyle;
    }
    const iri = pplModel.component.getIri(component);
    const execComponent = execModel.getComponent($execution, iri);
    if (execComponent === undefined) {
      return defaultRectStyle;
    }
    const status = execModel.getComponentStatus($execution, execComponent);

    if (status === LP.EXEC_RUNNING ||
      status === LP.EXEC_INITIALIZING) {
      return statusToRectStyle[status];
    }

    // Changed components are not available for mapping.
    if (execModel.isChanged($execution, execComponent)) {
      return statusToRectStyle["notAvailable"];
    }

    // Disabled mapping.
    if (!execModel.isMappingEnabled($execution, execComponent)) {
      if (status === LP.EXEC_FAILED) {
        return statusToRectStyle["disabled-failed"];
      } else if (status === LP.EXEC_CANCELLED) {
        return statusToRectStyle["disabled-mapped"];
      }
      return statusToRectStyle["disabled"];
    }
    return statusToRectStyle[status];
  }

  return {
    //  Implementation of canvas API.
    "getComponentLabel": pplModel.component.getLabel,
    "getComponentDescription": pplModel.component.getDescription,
    "getComponentFillColor": getComponentFillColor,
    "getComponentRectStyle": getComponentRectStyle,
    //
    "setPipeline": (pipeline) => $pipeline = pipeline,
    "setExecution": (execution) => $execution = execution,
  }

});





