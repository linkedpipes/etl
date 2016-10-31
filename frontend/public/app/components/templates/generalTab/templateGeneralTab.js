define([], function () {
    "use strict";

    const PARENT_PATH = "app/components/templates/generalTab/";

    function directiveFunction() {
        return {
            "restrict": "E",
            "scope": {
                "ngModel": "="
            },
            "templateUrl": PARENT_PATH + "templateGeneralTab.html",
            "link": (scope) => {

                scope.$watch("ngModel", (template) => {
                    if (template === undefined) {
                        scope.template = {};
                        return;
                    }
                    //
                    scope.template = template;
                    scope.color = template.color;
                    scope.useTemplateColor = template.color === undefined;
                });

                scope.onUseTemplateColor = () => {
                    if (scope.useTemplateColor) {
                        scope.template.color = undefined;
                    } else {
                        scope.template.color = scope.color;
                    }
                };

                scope.onColorChange = () => {
                    scope.template.color = scope.color;
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
        app.directive("lpTemplateGeneralTab", [directiveFunction]);
    };

});
