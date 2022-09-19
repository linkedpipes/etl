((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "../model/pipeline-model",
      "../model/execution-model",
      "./visual-service"
    ], definition);
  }
})((pplModel, execModel, visualService) => {

  let $pipeline;

  let $execution;

  function getComponentDescription(component) {
    const executionComponent = visualService.getExecutionComponent(
      pplModel, execModel, $execution, component);
    if (!executionComponent) {
      return pplModel.component.getDescription(component);
    }
    const durationMs = execModel.getComponentDurationMs(
      $execution, executionComponent);
    if (isNaN(durationMs)) {
      return "";
    }
    const durationS = Math.floor(durationMs / 1000);
    if (durationS === 0) {
      return "Duration: < 1s";
    }
    const durationM = Math.floor(durationS / 60);
    const durationH = Math.floor(durationM / 60);
    return "Duration: "
      + String(durationH % 60).padStart(2, '0') + ":"
      + String(durationM % 60).padStart(2, '0') + ":"
      + String(durationS).padStart(2, '0');
  }

  function getComponentFillColor(template, component) {
    const executionComponent = visualService.getExecutionComponent(
      pplModel, execModel, $execution, component);
    if (!executionComponent) {
      return "#f2f2f2";
    }
    const duration = execModel.getComponentDurationMs(
      $execution, executionComponent);
    const totalDuration = execModel.getTotalDurationMs(
      $execution, executionComponent);
    const componentCount = execModel.getComponentCount(
      $execution);
    const percentile = Math.floor((duration / totalDuration) * 100);
    const expectedPercentile = Math.floor((1.0 / componentCount) * 100);
    if (percentile < 2 * expectedPercentile) {
      // Using execution time for two components.
      return "#AAEEAA";
    } else if (percentile < 4 * expectedPercentile) {
      // Using execution time for four components.
      return "#EEEEAA";
    } else {
      // Using more execution time.
      return "#FFAAAA";
    }
  }

  function getComponentRectStyle(component) {
    return visualService.getComponentRectStyleByExecutionStatus(
      pplModel, execModel, $execution, component)
  }

  return {
    "name": "execution-time",
    //  Implementation of canvas API.
    "getComponentLabel": pplModel.component.getLabel,
    "getComponentDescription": getComponentDescription,
    "getComponentFillColor": getComponentFillColor,
    "getComponentRectStyle": getComponentRectStyle,
    //
    "setPipeline": (pipeline) => $pipeline = pipeline,
    "setExecution": (execution) => $execution = execution,
  }
});