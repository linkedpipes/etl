define([], function () {
  "use strict";

  function directiveFunction($location) {
    return {
      "restrict": "E",
      "scope": {
        "ngModel": "=",
        "showChildren": "=",
        "enableTemplateLink": "="
      },
      "template": require("./template-hierarchy-tab.html"),
      "link": ($scope) => {

        $scope.$watch("ngModel", (template) => {
          if (template === undefined) {
            $scope.template = {};
            return;
          }
          //
          $scope.template = template;
        });

        $scope.onTemplate = (template) => {
          $location.path("/templates/detail").search({
            "template": template.id
          });
        };

      }
    };
  }

  let _initialized = false;
  return function init(app) {
    if (_initialized) {
      return;
    }
    _initialized = true;
    //
    app.directive("lpTemplateHierarchyTab",
      ["$location", directiveFunction]);
  };

});
