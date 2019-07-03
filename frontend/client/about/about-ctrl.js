((definition) => {
  if (typeof define === "function" && define.amd) {
    define(["angular"], definition);
  }
})((angular) => {
  "use strict";

  function controller($scope) {

    function initialize() {
      $scope.commit = __GIT_COMMIT__;
    }

    angular.element(initialize);
  }

  controller.$inject = ["$scope"];

  let initialized = false;
  return function init(app) {
    if (initialized) {
      return;
    }
    initialized = true;
    app.controller("view-about", controller);
  }

});
