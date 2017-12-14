define([], () => {

    /**
     * HTML code for this controller is part of index.html file.
     */
    function controller($scope, $mdSidenav, $location, layout) {

        $scope.layout = layout;

        $scope.$on("$routeChangeSuccess", function (event, current, previous) {
            if (current.$$route && current.$$route.pageTitle) {
                $scope.index.title = current.$$route.pageTitle;
                if (current.$$route.color !== undefined) {
                    $scope.index.color = current.$$route.color;
                }
            }
        });

        const sidenavId = "left";
        $scope.toggleSidenav = function () {
            $mdSidenav(sidenavId).toggle();
        };

        $scope.closeSidenav = function () {
            $mdSidenav(sidenavId).close();
        };

        $scope.onPipelines = function () {
            $scope.closeSidenav();
            $location.path("/pipelines").search({});
        };

        $scope.onExecutions = function () {
            $scope.closeSidenav();
            $location.path("/executions").search({});
        };

        $scope.onTemplates = function () {
            $scope.closeSidenav();
            $location.path("/templates").search({});
        };

        $scope.onPersonalization = function () {
            $scope.closeSidenav();
            $location.path("/personalization").search({});
        };

        $scope.onHelp = function () {
            $scope.closeSidenav();
            $location.path("/help").search({});
        };

    }

    let _initialized = false;
    return function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        app.controller("layout-page",
            [
                "$scope",
                "$mdSidenav",
                "$location",
                "layout-service",
                controller]
        );
    };

});
