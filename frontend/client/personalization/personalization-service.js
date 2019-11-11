((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "./personalization",
      "@client/app-service/status"
    ], definition);
  }
})((_personalization, _status) => {

  function factory($personalization, $status) {

    let $scope;

    function initialize(scope) {
      $scope = scope;
      $scope.landingPage = $personalization.getLandingPage();
      $scope.initialListSize = $personalization.getListSize();
      $scope.pipelineOrdering = $personalization.getPipelineListOrder();
    }

    function save() {
      $personalization.setLandingPage($scope.landingPage);
      $personalization.setListSize($scope.initialListSize);
      $personalization.setPipelineListOrder($scope.pipelineOrdering);
      $status.success("Changes saved.");
    }

    return {
      "initialize": initialize,
      "save": save
    };
  }

  factory.$inject = [
    "personalization",
    "status"
  ];

  let initialized = false;
  return function init(app) {
    if (initialized) {
      return;
    }
    initialized = true;
    _personalization(app);
    _status(app);
    app.factory("view-personalization.service", factory);
  }
});





