((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "angular",
      "./execution-list-service",
      "../../app-service/refresh"
    ], definition);
  }
})((angular, _executionListService, _refreshService) => {
  "use strict";

  function controller($scope, $lpScrollWatch, service, $refresh) {

    service.initialize($scope);

    $scope.onExecute = service.executePipeline;

    $scope.onCancel = service.cancel;

    $scope.onOpenLogTail = service.openLogTail;

    $scope.onDelete = service.delete;

    $scope.$watch("filter.labelSearch", service.onSearchStringChange);

    $scope.$watch("filter.status", service.onSearchStateChange);

    $scope.noAction = () => {
      // This is do nothing action, we need it else the menu is open
      // on click to item. This cause menu to open which together
      // with navigation break the application.
    };

    let callbackReference = null;
    callbackReference = $lpScrollWatch.registerCallback(
      service.increaseVisibleItemLimit);
    $scope.$on("$destroy", () => {
      $lpScrollWatch.unRegisterCallback(callbackReference);
    });

    function initialize() {
      $lpScrollWatch.updateReference();
      service.load();
      $refresh.add("exec-list", service.update);
    }

    angular.element(initialize);
  }

  controller.$inject = [
    "$scope",
    "scrollWatch",
    "execution.list.service",
    "refresh"
  ];

  let initialized = false;
  return function init(app) {
    if (initialized) {
      return;
    }
    initialized = true;
    _executionListService(app);
    _refreshService(app);
    app.controller("components.executions.list", controller);
  }

});
