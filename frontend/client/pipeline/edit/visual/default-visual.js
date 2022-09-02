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

  function getComponentFillColor(template, component) {
    return visualService.getComponentTypeColor(
      pplModel, template, component);
  }

  function getComponentRectStyle(component) {
    return visualService.getComponentRectStyleByExecutionStatus(
      pplModel, execModel, $execution, component);
  }

  return {
    "name": "default",
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





