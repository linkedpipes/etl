((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "angular",
      "file-saver",
      "../app-service/jsonld/jsonld",
      "./export-pipeline-repository",
    ], definition);
  }
})((angular, saveAs, jsonld, _pipelineRepository,) => {
  "use strict";

  function factory($http, pipelineRepository, $status) {

    let $scope;

    function initialize(scope) {
      $scope = scope;
      $scope.pipelineFilter = {
        "labelSearch": ""
      };
      $scope.pipelineRepository =
        pipelineRepository.create($scope.pipelineFilter);
      $scope.working = false;
      $scope.exportAllPipelines = true;
      $scope.exportTemplates = true;
      $scope.removePrivateConfiguration = true;
      $scope.exportType = "ZIP_LABELS";
      //
      loadPipelines();
    }

    function loadPipelines() {
      pipelineRepository.load($scope.pipelineRepository).then(() => {
        $scope.pipelineLoaded = true;
      }).catch(angular.noop).then(() => $scope.$apply());
    }

    function onSearchStringChange() {
      pipelineRepository.onFilterChanged(
        $scope.pipelineRepository, $scope.pipeline);
    }

    function increaseVisibleItemLimit() {
      pipelineRepository.increaseVisibleItemLimit($scope.pipelineRepository);
    }

    function onExport() {
      let pipelinesForExport = [];
      if (!$scope.exportAllPipelines) {
        for (const pipeline of $scope.pipelineRepository.data) {
          if (pipeline.selected === true) {
            pipelinesForExport.push(pipeline.iri);
          }
        }
      }

      let url = "./api/v1/export";

      if ($scope.exportAllPipelines) {
        url += "?pipelines=ALL";
      } else {
        const exportList = JSON.stringify(pipelinesForExport);
        url += "?pipelines=" + encodeURIComponent(exportList);
      }

      if ($scope.exportTemplates) {
        url += "&templates=ALL";
      } else {
        url += "&templates=NONE";
      }

      if ($scope.removePrivateConfiguration) {
        url += "&removePrivateConfig=true";
      }

      url += "&exportType=" + $scope.exportType;

      fetch(url)
        .then(response => response.blob())
        .then(data => {
          let fileName = "export.zip";
          if ($scope.exportType === "FILE") {
            fileName = "export.trig";
          }
          saveAs(data, fileName)
        });

    }

    return {
      "initialize": initialize,
      "onSearchStringChange": onSearchStringChange,
      "increaseVisibleItemLimit": increaseVisibleItemLimit,
      "onExport": onExport
    };
  }

  factory.$inject = [
    "$http",
    "export.pipeline-repository",
    "status"
  ];

  let initialized = false;
  return function init(app) {
    if (initialized) {
      return;
    }
    initialized = true;
    _pipelineRepository(app);
    app.factory("export.service", factory);
  }
});