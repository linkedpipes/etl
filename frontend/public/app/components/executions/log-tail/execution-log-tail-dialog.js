define([], function () {

    const htmlPrefix = "log-tail-dialog-";

    function controller($scope, $mdDialog, $interval, $http, execution) {

        function setContent(data) {
            document.getElementById("log-tail-dialog-content").innerHTML = data;
        }

        function scrollToBottom() {
            const div = document.getElementById(htmlPrefix + "content-end");
            div.scrollIntoView(false);
        }

        const refreshData = () => {
            const url = "/resources/executions/" +
                execution.id + "/logs-tail?n=100";
            $http.get(url).then((response) => {
                    setContent(response.data);
                    scrollToBottom();
                }, (response) => {
                    console.error("Request failed.", response);
                }
            );
        };

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
