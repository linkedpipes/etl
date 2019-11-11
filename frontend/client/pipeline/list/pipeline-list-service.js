((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "@client/app-service/vocabulary",
      "angular",
      "./pipeline-list-repository",
      "./../pipeline-api"
    ], definition);
  }
})((vocabulary, angular, _repository, pipelineApi) => {

  function factory(
    $http, $location, $mdDialog, $status, $clipboard, repository) {

    let $scope;
    let $mdMedia;

    function initialize(scope, mdMedia) {
      $scope = scope;
      $mdMedia = mdMedia;
      $scope.isClipboardSupported = $clipboard.supported;
      $scope.filter = {
        "labelSearch": "",
        "tagsSearch": [],
        "tagsSearchString": "",
        "tagsAll": []
      };
      $scope.repository = repository.create($scope.filter);
    }

    function executePipeline(pipeline) {
      pipelineApi.executePipeline(
        $http, pipeline.iri, {"keepDebugData": true})
        .then(redirectToExecutionList)
        .catch(handleExecutionPostFailure);
    }

    function redirectToExecutionList() {
      $location.path("/executions").search({});
    }

    function handleExecutionPostFailure(response) {
      $status.httpError("Can't start the execution.", response);
    }

    function executeWithoutDebugData(pipeline) {
      pipelineApi.executePipeline(
        $http, pipeline.iri, {"keepDebugData": false})
        .then(redirectToExecutionList)
        .catch(handleExecutionPostFailure);
    }

    function createPipeline() {
      pipelineApi.create($http)
        .then(redirectToPipelineDetail)
        .catch(handleCreatePipelineFailure);
    }

    function redirectToPipelineDetail(response) {
      const iri = response.data[0]["@graph"][0]["@id"];
      $location.path("/pipelines/edit/canvas").search({
        "pipeline": iri
      });
    }

    function handleCreatePipelineFailure(response) {
      $status.httpError("Can't create the pipeline.", response);
    }

    function redirectToPipelineUpload() {
      $location.path("/pipelines/upload").search({});
    }

    function copyPipelineIriToClipboard(pipeline) {
      $clipboard.copyText(pipeline.iri)
    }

    function copyPipeline(pipeline) {
      // TODO As we remove $http we need to notify $scope manually.
      pipelineApi.copy($http, pipeline)
        .then(handleCopyPipelineSuccess)
        .catch(reportCopyPipelineFailure)
    }

    function handleCopyPipelineSuccess() {
      $status.success("Pipeline has been successfully copied.");
      return repository.update($scope.repository);
    }

    function reportCopyPipelineFailure(response) {
      $status.httpError("Can't create the pipeline.", response);
    }

    function deletePipeline(pipeline, event) {
      const dialogText = "Would you like to delete pipeline '"
        + pipeline.label + "'?";

      const confirmDialog = $mdDialog.confirm()
        .title(dialogText)
        .ariaLabel("Delete pipeline.")
        .targetEvent(event)
        .ok("Delete pipeline")
        .cancel("Cancel");

      $mdDialog.show(confirmDialog)
        .then(() => {
            // This is out of angular scope.
            repository.delete($scope.repository, pipeline)
              .then(() => $scope.$apply());
          },
          () => {
            // No action.
          }
        );
    }

    function onChipsFilterChange() {
      repository.onFilterChanged($scope.repository, "chips");
    }

    function onSearchStringChange() {
      repository.onFilterChanged($scope.repository, "label");
    }

    function loadPipelines() {
      return repository
        .load($scope.repository)
        .catch(angular.noop)
        .then(() => {
          $scope.$apply()
        });
    }

    function getTagsMatchingQuery(query) {
      query = query.toLowerCase();
      return $scope.filter.tagsAll.filter((item) => {
        return item.toLowerCase().indexOf(query) !== -1;
      });
    }

    function increaseVisibleItemLimit(byButton) {
      repository.increaseVisibleItemLimit($scope.repository);
      if (!byButton) {
        $scope.$apply();
      }
    }

    return {
      "initialize": initialize,
      "executePipeline": executePipeline,
      "executeWithoutDebugData": executeWithoutDebugData,
      "create": createPipeline,
      "redirectToUpload": redirectToPipelineUpload,
      "copy": copyPipeline,
      "copyIri": copyPipelineIriToClipboard,
      "delete": deletePipeline,
      "onChipsFilterChange": onChipsFilterChange,
      "onSearchStringChange": onSearchStringChange,
      "load": loadPipelines,
      "getTagsMatchingQuery": getTagsMatchingQuery,
      "increaseVisibleItemLimit": increaseVisibleItemLimit
    };
  }

  factory.$inject = [
    "$http",
    "$location",
    "$mdDialog",
    "status",
    "clipboard",
    "view-pipelines-list.repository"
  ];

  let initialized = false;
  return function init(app) {
    if (initialized) {
      return;
    }
    initialized = true;
    _repository(app);
    app.factory("view-pipelines-list.service", factory);
  }
});