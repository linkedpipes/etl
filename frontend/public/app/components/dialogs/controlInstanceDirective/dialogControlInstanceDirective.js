define([], function () {
    "use strict";

    const PARENT_PATH = "app/components/dialogs/controlInstanceDirective/";

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
            "templateUrl": PARENT_PATH + "dialogControlInstanceDirective.html",
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

    var _initialized = false;
    return function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;

        app.directive("lpDialogControlInstance",
            [directiveFunction]);
    };

});
