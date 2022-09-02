// This file provides functionality used in visuals, it is here
// so visuals can easily share it.

import {LP} from "@client/app-service/vocabulary";

export function getComponentTypeColor(
  pipelineModel, template, component) {
  let color = pipelineModel.component.getColor(component);
  if (color === undefined) {
    color = getEffectiveColor(template);
  }
  if (pipelineModel.component.isDisabled(component)) {
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

export function getComponentRectStyleByExecutionStatus(
  pipelineModel, executionModel, execution, component) {
  const execComponent = getExecutionComponent(
    pipelineModel, executionModel, execution, component);
  if (execComponent === undefined) {
    return defaultRectStyle;
  }
  const status = executionModel.getComponentStatus(execution, execComponent);
  if (status === LP.EXEC_RUNNING || status === LP.EXEC_INITIALIZING) {
    return statusToRectStyle[status];
  }
  // Changed components are not available for mapping.
  if (executionModel.isChanged(execution, execComponent)) {
    return statusToRectStyle["notAvailable"];
  }
  // Disabled mapping.
  if (!executionModel.isMappingEnabled(execution, execComponent)) {
    if (status === LP.EXEC_FAILED) {
      return statusToRectStyle["disabled-failed"];
    } else if (status === LP.EXEC_CANCELLED) {
      return statusToRectStyle["disabled-mapped"];
    }
    return statusToRectStyle["disabled"];
  }
  return statusToRectStyle[status];
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

export function getExecutionComponent(
  pipelineModel, executionModel, execution, component
) {
  if (!execution) {
    return undefined;
  }
  const iri = pipelineModel.component.getIri(component);
  return executionModel.getComponent(execution, iri);
}
