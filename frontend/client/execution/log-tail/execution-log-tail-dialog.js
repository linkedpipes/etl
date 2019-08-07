define([], function () {

  const htmlPrefix = "log-tail-dialog-";

  function controller($scope, $mdDialog, $interval, $http, $refresh, execution) {

    function scrollToBottom() {
      const div = document.getElementById(htmlPrefix + "content-end");
      div.scrollIntoView(false);
    }

    function isExecutionFinished(execution) {
      return execution["status-monitor"] ===
        "http://etl.linkedpipes.com/resources/status/finished";
    }

    const refreshData = () => {
      const element = document.getElementById(htmlPrefix + "content");
      if (element === null || element === undefined) {
        // Dialog was closed.
        $refresh.remove("log-tail");
        return;
      }
      const url = execution.iri + "/logs-tail?n=100";
      $http.get(url).then((response) => {
          const lastSize = element.textContent.length;
          element.textContent = response.data;
          if (response.data.length !== lastSize) {
            scrollToBottom();
          }
          if (isExecutionFinished(execution)) {
            $interval.cancel(refreshStop);
          }
        }, (response) => {
        element.textContent = "There are no log data available.";
          console.warn("Request failed.", response);
        }
      );
    };

    const refreshStop = $interval(refreshData, 5000);
    $scope.label = execution.label;

    $scope.onClose = function () {
      $interval.cancel(refreshStop);
      $refresh.remove("log-tail");
      $mdDialog.hide();
    };

    refreshData();
    $refresh.add("log-tail", refreshData);
  }

  controller.$inject = [
    "$scope",
    "$mdDialog",
    "$interval",
    "$http",
    "refresh",
    "execution"
  ];

  let _initialized = false;
  return function init(app) {
    if (_initialized) {
      return;
    }
    _initialized = true;
    app.controller("execution.log-tail.dialog", controller);
  }

});
