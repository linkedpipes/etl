define([], () => {

    /**
     * HTML code for this controller is part of index.html file.
     */
    function controller($scope, $mdSidenav, $location, layout) {

        $scope.layout = layout;

        $scope.$on("$routeChangeSuccess", (event, current, previous) => {
            if (current.$$route && current.$$route.pageTitle) {
                $scope.layout.title = current.$$route.pageTitle;
                if (current.$$route.color !== undefined) {
                    $scope.layout.color = current.$$route.color;
                }
            }
        });

        const sidenavId = "left";
        $scope.toggleSidenav = () => {
            $mdSidenav(sidenavId).toggle();
        };

        $scope.closeSidenav = () => {
            $mdSidenav(sidenavId).close();
        };

        $scope.onPipelines = () => {
            $scope.closeSidenav();
            $location.path("/pipelines").search({});
        };

        $scope.onExecutions = () => {
            $scope.closeSidenav();
            $location.path("/executions").search({});
        };

        $scope.onTemplates = () => {
            $scope.closeSidenav();
            $location.path("/templates").search({});
        };

        $scope.onPersonalization = () => {
            $scope.closeSidenav();
            $location.path("/personalization").search({});
        };

        $scope.onHelp = () => {
            $scope.closeSidenav();
            $location.path("/help").search({});
        };

    }

    controller.$inject = [
        "$scope",
        "$mdSidenav",
        "$location",
        "layout-service"
    ];

    let _initialized = false;
    return function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        app.controller("layout-page", controller);
    };

});
