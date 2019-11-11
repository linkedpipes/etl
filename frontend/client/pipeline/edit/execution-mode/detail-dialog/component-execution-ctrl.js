((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "@client/app-service/jsonld/jsonld",
      "./component-execution-service"
    ], definition);
  }
})((jsonld, _service) => {
  "use strict";

  function controller(
    $scope, $mdDialog, $service, component, execution, iri) {

    $service.initialize($scope, $mdDialog, component, execution, iri);

    $scope.onCancel = $service.onCancel;

    $scope.$on("$routeChangeStart", function ($event, next, current) {
      $mdDialog.cancel();
    });
  }

  controller.$inject = [
    "$scope", "$mdDialog", "components.component.execution.service",
    "component", "execution", "executionIri"];

  let initialized = false;
  return function init(app) {
    if (initialized) {
      return;
    }
    initialized = true;
    _service(app);
    app.controller("components.component.execution.dialog", controller);
  }
});