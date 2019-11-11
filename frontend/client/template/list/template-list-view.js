define([
  "../template-service"
], function (_template_service) {
  "use strict";

  function controller($scope, $location, templateService) {

    $scope.templates = [];

    $scope.loaded = false;

    $scope.onClick = (template) => {
      $location.path("/templates/detail").search({
        "template": template.id
      });
    };

    $scope.$watch("searchLabel", (newValue, oldValue) => {
      if (newValue === oldValue) {
        return;
      }
      if (newValue === "") {
        // No filter, just set all to visible.
        $scope.templates.forEach((template) => {
          template._templateListView = true;
        });
        return;
      }
      var query = new RegExp(
        newValue.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, "\\$&"), "i");
      $scope.templates.forEach((template) => {
        template._templateListView = query.test(template.label);
      });
    });

    (function init() {
      templateService.load().then(() => {
        const templateList = templateService.getTemplatesList();
        templateList.forEach((template) => {
          // Filter out core templates.
          if (template.isCore) {
            return;
          }
          template._templateListView = true;
          $scope.templates.push(template);
        });
        $scope.loaded = true;
        $scope.error = undefined;
      }).catch((error) => {
        $scope.error = error["statusText"];
        console.log("Can't load templates.", error);
      });
    })();

  }

  let initialized = false;
  return function init(app) {
    if (initialized) {
      return;
    }
    _template_service(app);
    app.controller("template.list",
      ["$scope", "$location", "template.service", controller]);
  };

});
