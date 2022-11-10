((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "../../app-service/vocabulary",
      "angular",
      "./import-response-model"
    ], definition);
  }
})((vocabulary, angular, importResponse) => {

  function factory($http, $timeout, $location, $status, $templates) {

    const VIEW_INPUT = "input";

    const VIEW_UPLOADING = "uploading";

    const VIEW_RESULT = "done";

    const PIPELINE_EDIT_URL = "#/pipelines/edit/canvas?pipeline=";

    const TEMPLATE_EDIT_URL = "#/templates/detail?template=";

    let $scope;

    function initialize(scope) {
      $scope = scope;
      //
      $scope.view = VIEW_INPUT;
      $scope.fileReady = false;
      $scope.type = "file";
      $scope.importTemplates = true;
      $scope.updateTemplates = false;
      $scope.keepPipelineSuffix = false;
      $scope.uploadResult = {};
    }

    function onWatchFile() {
      if (!$scope.file) {
        $scope.fileReady = false;
      } else if ($scope.file.$error) {
        $scope.fileReady = false;
      } else {
        $scope.fileReady = true;
      }
    }

    function onUpload() {
      $scope.view = VIEW_UPLOADING;
      if ($scope.type === "file") {
        if (!$scope.fileReady) {
          return;
        }
        importFile();
      } else if ($scope.type === "url") {
        importUrl();
      } else {
        console.log("Unknown type.");
      }
    }

    function importFile() {
      const data = new FormData();
      const options = createImportOptions();
      data.append("options", new Blob([JSON.stringify(options)], {
        "type": "application/ld+json"
      }), "options.jsonld");
      data.append("pipeline", $scope.file);
      const url = "./api/v1/import";
      postFormData(url, data);
    }

    function createImportOptions() {
      return {
        "@id": "http://localhost/options",
        "@type": "http://linkedpipes.com/ontology/UpdateOptions",
        "http://etl.linkedpipes.com/ontology/importPipeline": "true",
        "http://etl.linkedpipes.com/ontology/keepPipelineSuffix": $scope.keepPipelineSuffix,
        "http://etl.linkedpipes.com/ontology/importNewTemplates": $scope.importTemplates,
        "http://etl.linkedpipes.com/ontology/updateExistingTemplates": $scope.updateTemplates,
      };
    }

    function postFormData(url, data) {
      const config = {
        "transformRequest": angular.identity,
        "headers": {
          // Content-Type is set by Angular.
          "Content-Type": undefined,
          "accept": "application/ld+json"
        }
      };
      $http.post(url, data, config).then((response) => {
        reloadTemplates()
          .catch(() => $status.httpError("Can't reload templates.", response))
          .then(() => handleResponse(response));
      }, (error) => {
        $status.httpError("Import failed.", error)
        $scope.view = VIEW_INPUT;
      });
    }

    function reloadTemplates() {
      return $templates.forceLoad();
    }

    function handleResponse(response) {

      const responseModel = importResponse.parseImportResponse(response.data);
      const pipelines = [];
      responseModel.pipelines.forEach(item => {
        if (!item.stored) {
          return;
        }
        pipelines.push({
          "url": item.local,
          "label": item.label,
          "tags": item.tags,
          "onClickUrl": PIPELINE_EDIT_URL + encodeURIComponent(item.local)
        });
      });
      const referenceTemplates = [];
      responseModel.referenceTemplates.forEach(item => {
        if (!item.stored) {
          return;
        }
        referenceTemplates.push({
          "url": item.local,
          "label": item.label,
          "tags": item.tags,
          "onClickUrl": TEMPLATE_EDIT_URL + encodeURIComponent(item.local)
        });
      });
      if (pipelines.length === 1) {
        // For backwards compatibility.
        $location.path("/pipelines/edit/canvas").search({
          "pipeline": pipelines[0]["url"]
        });
      }
      $scope.uploadResult = {
        "pipelines": pipelines,
        "referenceTemplates": referenceTemplates
      };
      $scope.view = VIEW_RESULT;
    }

    function importUrl() {
      const data = new FormData();
      const options = createImportOptions();
      data.append("options", new Blob([JSON.stringify(options)], {
        "type": "application/ld+json"
      }), "options.jsonld");
      const url = "./api/v1/import?iri=" +
        encodeURIComponent($scope.url);
      postFormData(url, data);
    }

    return {
      "initialize": initialize,
      "onUpload": onUpload,
      "onWatchFile": onWatchFile
    };
  }

  factory.$inject = [
    "$http",
    "$timeout",
    "$location",
    "status",
    "template.service"
  ];

  let initialized = false;
  return function init(app) {
    if (initialized) {
      return;
    }
    initialized = true;
    app.factory("pipeline.upload.service", factory);
  }

});