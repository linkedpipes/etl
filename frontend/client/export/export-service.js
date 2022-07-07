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
        url += "?pipelines=all";
      } else {
        const exportList = JSON.stringify(pipelinesForExport);
        url += "?pipelines=" + encodeURIComponent(exportList);
      }

      if ($scope.exportTemplates) {
        url += "&templates=all";
      } else {
        url += "&templates=none";
      }

      if ($scope.removePrivateConfiguration) {
        url += "&removePrivateConfig=true";
      }

      fetch(url).then(response => response.blob())
        .then(data => saveAs(data, "export.zip"));

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