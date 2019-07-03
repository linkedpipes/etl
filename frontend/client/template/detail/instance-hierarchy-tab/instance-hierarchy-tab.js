define([], function () {
  "use strict";

  function directive() {
    return {
      "restrict": "E",
      "scope": {
        "instance": "=",
        "parent": "="
      },
      "template": require("./instance-hierarchy-tab.html"),
      "link": ($scope) => {

        $scope.$watch("parent", (parent) => {
          if (parent === undefined) {
            $scope.parent = {};
          }
        });
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
    app.directive("lpInstanceHierarchyTab", [directive]);
  };

});
