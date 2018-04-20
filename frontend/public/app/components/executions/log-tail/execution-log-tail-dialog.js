define([], function () {

    const htmlPrefix = "log-tail-dialog-";

    function controller($scope, $mdDialog, $interval, $http, execution) {

        function scrollToBottom() {
            const div = document.getElementById(htmlPrefix + "content-end");
            div.scrollIntoView(false);
        }

        function isExecutionFinished(execution) {
            return execution["status-monitor"] ==
                "http://etl.linkedpipes.com/resources/status/finished";
        }

        const refreshData = () => {
            const url = "/resources/executions/" +
                execution.id + "/logs-tail?n=100";
            $http.get(url).then((response) => {
                    setContent(response.data);
                    scrollToBottom();
                    if (isExecutionFinished(execution)) {
                        $interval.cancel(refreshStop);
                    }
                }, (response) => {
                    setContent("There are no log data available.");
                    console.warn("Request failed.", response);
                }
            );
        };

        function setContent(data) {
            const element = document.getElementById(htmlPrefix + "content");
            element.textContent = data;
        }

        const refreshStop = $interval(refreshData, 5000);
        $scope.label = execution.label;

        $scope.onClose = function () {
            $interval.cancel(refreshStop);
            $mdDialog.hide();
        };

        refreshData();
    }

    controller.$inject = [
        "$scope",
        "$mdDialog",
        "$interval",
        "$http",
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
