define([], function () {
    "use strict";

    const PARENT_PATH = "app/components/dialogs/controlInstanceDirective/";

    function directiveFunction() {
        return {
            "restrict": "A",
            "scope": {
                "ngModel": "=",
            },
            "templateUrl": PARENT_PATH + "dialogControlInstanceDirective.html",
            "link": (scope) => {

                scope.$watch("ngModel", (model) => {
                    if (model === undefined) {
                        scope.model = {};
                        return;
                    }
                    //
                    scope.model = model;
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

        app.directive("lpDialogControlInstance",
            [directiveFunction]);
    };

});
