define([], function () {
    "use strict";

    const PARENT_PATH = "app/components/templates/hierarchyTab/";

    function directiveFunction($location) {
        return {
            "restrict": "E",
            "scope": {
                "ngModel": "="
            },
            "templateUrl": PARENT_PATH + "templateHierarchyTab.html",
            "link": (scope) => {

                scope.$watch("ngModel", (template) => {
                    if (template === undefined) {
                        scope.template = {};
                        return;
                    }
                    //
                    scope.template = template;
                });

                scope.onTemplate = (template) => {
                    $location.path("/templates/detail").search({
                        "template": template.id
                    });
                };

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
        app.directive("lpTemplateHierarchyTab",
            ['$location', directiveFunction]);
    };

});
