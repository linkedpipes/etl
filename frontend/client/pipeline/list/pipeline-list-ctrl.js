import angular from "angular";
import serviceRegistration from "./pipeline-list-service";

function controller(
  $scope, $mdMedia, $lpScrollWatch, $http, $location, $mdDialog,
  $status, $clipboard, service
) {

  service.initialize($scope, $mdMedia);

  $scope.getTagsMatchingQuery = service.getTagsMatchingQuery;

  $scope.onExecute = service.executePipeline;

  $scope.onExecuteWithoutDebugData = service.executeWithoutDebugData;

  $scope.onCreate = service.create;

  $scope.onUpload = service.redirectToUpload;

  $scope.onCopy = service.copy;

  $scope.onCopyIri = service.copyIri;

  $scope.onDelete = service.delete;

  $scope.chipsFilter = service.onChipsFilterChange;

  $scope.$watch("filter.labelSearch", service.onSearchStringChange);

  $scope.noAction = () => {
    // This is a do nothing action, we need it else the menu is open
    // on click to item. This cause menu to open which together
    // with navigation break the application.
  };

  let callbackReference = null;

  callbackReference = $lpScrollWatch.registerCallback(
    service.increaseVisibleItemLimit);

  $scope.$on("$destroy", () => {
    $lpScrollWatch.unRegisterCallback(callbackReference);
  });

  angular.element(function initialize() {
    $lpScrollWatch.updateReference();
    service.load();
  });
}

controller.$inject = [
  "$scope",
  "$mdMedia",
  "scrollWatch",
  "$http",
  "$location",
  "$mdDialog",
  "status",
  "clipboard",
  "pipelines-list.service",
];

let initialized = false;

export default function register(app) {
  if (initialized) {
    return;
  }
  initialized = true;
  serviceRegistration(app);
  app.controller("pipelines-list.controller", controller);
}
