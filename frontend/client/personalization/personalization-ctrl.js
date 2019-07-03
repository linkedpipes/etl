((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "./personalization-service"
    ], definition);
  }
})((_personalizationService) => {
  "use strict";

  function controller($scope, service) {

    service.initialize($scope);

    $scope.onDiscard = service.load;

    $scope.onSave = service.save;

  }

  controller.$inject = ["$scope", "view-personalization.service"];

  let initialized = false;
  return function init(app) {
    if (initialized) {
      return;
    }
    initialized = true;
    _personalizationService(app);
    app.controller("view-personalization", controller);
  }

});
