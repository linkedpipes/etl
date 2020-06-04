((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "../../app-service/vocabulary",
      "angular",
      "./execution-list-repository",
      "../log-tail/execution-log-tail-dialog",
      "./../execution-api",
      "../../pipeline/pipeline-api",
    ], definition);
  }
})((vocabulary, angular, _repository, _logTailDialog, executionApi, pipelineApi) => {

  const LP = vocabulary.LP;

  function factory($http, $mdDialog, $status, $mdMedia, repository) {

    let $scope;

    function initialize(scope) {
      $scope = scope;
      $scope.filter = {
        "labelSearch": "",
        "status": "",
      };
      $scope.pipelineStates = [
        {
          "label": "Running",
          "filter": [LP.EXEC_INITIALIZING, LP.EXEC_RUNNING],
        },
        {
          "label": "Finished",
          "filter": [LP.EXEC_FINISHED],
        },
        {
          "label": "Failed",
          "filter": [LP.EXEC_FAILED],
        },
        {
          "label": "Queued",
          "filter": [LP.EXEC_QUEUED],
        },
        {
          "label": "Cancelling",
          "filter": [LP.EXEC_CANCELLING],
        },
        {
          "label": "Cancelled",
          "filter": [LP.EXEC_CANCELLED],
        },
      ];
      $scope.repository = repository.create($scope.filter);
    }

    function execute(execution) {
      const iri = execution["pipeline"]["iri"];
      const options = {
        "keepDebugData": true
      };
      pipelineApi.executePipeline($http, iri, options)
        .then(updateRepository)
        .catch(handleExecutionStartFailure);
    }

    function updateRepository() {
      repository.update($scope.repository)
        .catch(angular.noop);
    }

    function handleExecutionStartFailure(response) {
      $status.httpError("Can't start the execution.", response);
    }

    function cancelExecution(execution) {
      executionApi.cancel($http, execution.iri, "User request")
        .then(() => {
          execution.cancelling = true;
        }).catch(handleExecutionCancelFailure);
    }

    function handleExecutionCancelFailure(response) {
      $status.httpError("Can't cancel the execution.", response);
    }

    function openLogTail(execution) {
      const useFullScreen = ($mdMedia("sm") || $mdMedia("xs"));
      $mdDialog.show({
        "controller": "execution.log-tail.dialog",
        "template": require("./../log-tail/execution-log-tail-dialog.html"),
        "clickOutsideToClose": false,
        "fullscreen": useFullScreen,
        "locals": {
          "execution": execution
        }
      });
    }

    // TODO Consider adding confirmation dialog.
    function deleteExecution(execution, event) {
      repository.delete(execution, $scope.repository);
    }

    function onSearchStringChange() {
      repository.onFilterChanged($scope.repository, "label");
    }

    function onSearchStateChange() {
      repository.onFilterChanged($scope.repository, "status");
    }

    function loadExecutions() {
      repository.load($scope.repository)
        .catch(angular.noop)
        .then(() => $scope.$apply());
    }

    function increaseVisibleItemLimit(byButton) {
      repository.increaseVisibleItemLimit($scope.repository);
      if (!byButton) {
        // This event come outside of Angular scope.
        $scope.$apply();
      }
    }

    return {
      "initialize": initialize,
      "executePipeline": execute,
      "cancel": cancelExecution,
      "openLogTail": openLogTail,
      "delete": deleteExecution,
      "onSearchStringChange": onSearchStringChange,
      "onSearchStateChange": onSearchStateChange,
      "increaseVisibleItemLimit": increaseVisibleItemLimit,
      "load": loadExecutions,
      "update": updateRepository
    };
  }

  factory.$inject = [
    "$http",
    "$mdDialog",
    "status",
    "$mdMedia",
    "execution.list.repository"];

  let initialized = false;
  return function init(app) {
    if (initialized) {
      return;
    }
    initialized = true;
    _repository(app);
    _logTailDialog(app);
    app.factory("execution.list.service", factory);
  }
});





