import angular from "angular";

function controller($scope) {

  function initialize() {
    $scope.commit = __GIT_COMMIT__;
  }

  angular.element(initialize);
}

controller.$inject = ["$scope"];

let initialized = false;

export default function register(app) {
  if (initialized) {
    return;
  }
  initialized = true;
  app.controller("view-about", controller);
}
