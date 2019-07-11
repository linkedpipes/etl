define([
  "@client/app-service/scroll-watch",
  "@client/app-service/server-status",
  "./layout-service"
], (_scrollWatch, serverStatus, _layoutService) => {

  function directive() {
    return {
      "restrict": "E",
      "replace": true,
      "template": require("./layout-view.html"),
      "controller": controller
    }
  }

  function controller($scope, $mdSideBar, $location, service) {

    // Make content of layout reactive.
    $scope.instanceLabel = serverStatus.getStatus().label;
    $scope.pageTitle = "";
    $scope.layout = service;

    serverStatus.onLoad((status) => {
      $scope.instanceLabel = status.label;
      $scope.$digest();
      updateDocumentTitle();
    });

    function updateDocumentTitle() {
      let newDocumentTitle = "";
      if ($scope.pageTitle !== "") {
        newDocumentTitle = $scope.pageTitle + " - ";
      }
      newDocumentTitle += $scope.instanceLabel;
      document.title = newDocumentTitle;
    }

    $scope.$on("$routeChangeSuccess", (event, current, previous) => {
      if (!current.$$route || !current.$$route.pageTitle) {
        return;
      }
      if (current.$$route.color !== undefined) {
        $scope.layout.color = current.$$route.color;
      }
      $scope.pageTitle = current.$$route.pageTitle;
      updateDocumentTitle()
    });

    const sideNavigationBarName = "left";
    $scope.toggleSidenav = () => {
      $mdSideBar(sideNavigationBarName).toggle();
    };

    $scope.closeSidenav = () => {
      $mdSideBar(sideNavigationBarName).close();
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

  controller.$inject = ["$scope", "$mdSidenav", "$location", "app-layout.service"];

  let _initialized = false;
  return function init(app) {
    if (_initialized) {
      return;
    }
    _initialized = true;
    _scrollWatch(app);
    _layoutService(app);
    app.directive("lpApplication", directive);
  };

});
