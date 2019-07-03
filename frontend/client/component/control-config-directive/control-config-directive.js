define([], function () {
  "use strict";

  // TODO Compute in parent component.
  function areAllOptionsForced(model) {
    for (let key in model) {
      if (!model.hasOwnProperty(key)) {
        continue;
      }
      if (!model[key].forced) {
        return false;
      }
    }
    return true;
  }

  function directiveFunction() {
    return {
      "restrict": "E",
      "scope": {
        "dialog": "=lpDialog",
        "application": "=lpApplication"
      },
      "template": require("./control-config-directive.html"),
      "link": (scope) => {
        scope.$watch("dialog", (model) => {
          if (model === undefined) {
            scope.model = {};
            return;
          }
          //
          scope.model = model;
          scope.allForced = areAllOptionsForced(model);
        });

        scope.changeToHierarchyTab = function () {
          scope.application.changeToHierarchyTab();
        }

      }
    };
  }

  let _initialized = false;
  return function init(app) {
    if (_initialized) {
      return;
    }
    _initialized = true;

    app.directive("lpDialogControlConfig", [directiveFunction]);
  };

});
