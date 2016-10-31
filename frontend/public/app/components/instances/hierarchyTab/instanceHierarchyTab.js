define([], function () {
    "use strict";

    const PARENT_PATH = "app/components/instances/hierarchyTab/";

    function directive(templateService) {
        return {
            "restrict": "E",
            "scope": {
                "instance": "=",
                "parent": "="
            },
            "templateUrl": PARENT_PATH + "instanceHierarchyTab.html",
            "link": (scope) => {

                scope.$watch("parent", (parent) => {
                    if (parent === undefined) {
                        scope.parent = {};
                        return;
                    }
                });
            }
        };
    }

    var _initialized = false;
    return function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        //
        app.directive("lpInstanceHierarchyTab",
            ["template.service", directive]);
    };

});
