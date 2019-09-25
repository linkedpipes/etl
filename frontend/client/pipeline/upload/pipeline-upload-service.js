((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "../../app-service/vocabulary",
      "angular"
    ], definition);
  }
})((vocabulary, angular) => {

  function factory($http, $timeout, $location, $status, $templates) {

    let $scope;

    function initialize(scope) {
      $scope = scope;
      //
      $scope.fileReady = false;
      $scope.uploading = false;
      $scope.log = "";
      $scope.type = "file";
      $scope.importTemplates = true;
      $scope.updateTemplates = false;
    }

    function onWatchFile() {
      if (!$scope.file) {
        $scope.fileReady = false;
        return;
      } else if ($scope.file.$error) {
        $scope.fileReady = false;
      } else {
        $scope.fileReady = true;
      }
    }

    function onUpload() {
      $scope.uploading = true;
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
      const url = "./resources/pipelines";
      postFormData(url, data);
    }

    function createImportOptions() {
      return {
        "@id": "http://localhost/options",
        "@type": "http://linkedpipes.com/ontology/UpdateOptions",
        "http://etl.linkedpipes.com/ontology/local": "false",
        "http://etl.linkedpipes.com/ontology/importTemplates": $scope.importTemplates,
        "http://etl.linkedpipes.com/ontology/updateTemplates": $scope.updateTemplates
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
        reloadTemplates().then(() => {
          redirectToPipeline(response);
        }).catch(() => {
          $status.httpError("Can't update templates.", response);
          redirectToPipeline(response);
        });
      }, (error) => {
        $status.httpError("Can't copy pipeline.", error);
      });
    }

    function reloadTemplates() {
      return $templates.forceLoad();
    }

    function redirectToPipeline(response) {
      $location.path("/pipelines/edit/canvas").search({
        "pipeline": response.data[0]["@graph"][0]["@id"]
      });
    }

    function importUrl() {
      const data = new FormData();
      const options = createImportOptions();
      data.append("options", new Blob([JSON.stringify(options)], {
        "type": "application/ld+json"
      }), "options.jsonld");
      const url = "./resources/pipelines?pipeline=" +
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