((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "./export-service"
    ], definition);
  }
})((_service) => {
  "use strict";

  function controller($scope, $lpScrollWatch, service) {

    service.initialize($scope);

    $scope.$watch("pipelineFilter.labelSearch",
      service.onSearchStringChange);

    $scope.onExport = () => service.onExport();

    let callbackReference = $lpScrollWatch.registerCallback((byButton) => {
      service.increaseVisibleItemLimit();
      if (!byButton) {
        // This event come outside Angular scope.
        $scope.$apply();
      }
    });

    $scope.$on("$destroy", () => {
      $lpScrollWatch.unRegisterCallback(callbackReference);
    });

    function initialize() {
      $lpScrollWatch.updateReference();
    }

    angular.element(initialize);
  }

  controller.$inject = [
    "$scope",
    "scrollWatch",
    "export.service"
  ];

  let initialized = false;
  return function init(app) {
    if (initialized) {
      return;
    }
    initialized = true;
    _service(app);
    app.controller("view-export", controller);
  }

});
