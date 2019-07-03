((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "angular",
      "./pipeline-upload-service"
    ], definition);
  }
})((angular, _pipelineUploadService) => {
  "use strict";

  function controller($scope, service) {

    service.initialize($scope);

    $scope.$watch("file", service.onWatchFile);

    $scope.onUpload = service.onUpload;

  }

  controller.$inject = [
    "$scope",
    "pipeline.upload.service"
  ];

  let initialized = false;
  return function init(app) {
    if (initialized) {
      return;
    }
    initialized = true;
    _pipelineUploadService(app);
    app.controller("components.pipelines.upload", controller);
  }
});