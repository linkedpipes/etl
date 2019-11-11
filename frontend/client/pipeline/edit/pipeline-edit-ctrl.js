((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "./pipeline-edit-service",
      "./canvas/canvas-directive",
      "./edit-mode/edit-directive/canvas-edit-directive"
    ], definition);
  }
})((_service, _canvasDirective, _editDirective) => {
  "use strict";

  function controller($scope, $timeout, $routeParams, $service) {

    $service.initialize(
      $scope,
      $routeParams.pipeline,
      $routeParams.execution);

    $scope.onPipelineDetail = $service.onPipelineDetail;

    $scope.onOpenMenu = ($mdOpenMenu, event) => $mdOpenMenu(event);

    $scope.onDisableAllLoaders = $service.onDisableAllLoaders;

    $scope.onEnableAllLoaders = $service.onEnableAllLoaders;

    $scope.onDownload = $service.onDownload;

    $scope.onDownloadNoCredentials = $service.onDownloadNoCredentials;

    $scope.onBackupDownload = $service.onBackupDownload;

    $scope.onExecuteWithoutDebugData = $service.onExecuteWithoutDebugData;

    $scope.onCopy = $service.onCopyPipeline;

    $scope.onDelete = $service.onDelete;

    $scope.onSave = $service.onSave;

    $scope.onExecute = $service.onExecute;

    $scope.onClose = $service.onClose;

    $scope.onEditMode = $service.onEditMode;

    $scope.onDownloadNoSave = $service.onDownloadNoSave;

    $scope.onDownloadNoCredentialsNoSave = $service.onDownloadNoCredentialsNoSave;

    $scope.onCopyNoSave = $service.onCopyNoSave;

    $timeout($service.onHtmlReady);

  }

  controller.$inject = [
    "$scope", "$timeout", "$routeParams",
    "components.pipeline.canvas.service"
  ];

  let initialized = false;
  return function init(app) {
    if (initialized) {
      return;
    }
    initialized = true;
    _service(app);
    _canvasDirective(app);
    _editDirective(app);
    app.controller("components.pipeline.canvas.view", controller);
  }

});
