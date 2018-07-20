/**
 * Directive for list of IRIs.
 *
 * The ngModel must be set to the value of predicate in JSON-LD.
 */
define(["jquery"], function (jQuery) {
    "use strict";

    function directive() {
        return {
            require: "ngModel",
            scope: {
                "topLabel": "@labelTop",
                "itemLabel": "@labelItem",
                "disabled": "=lpDisabled"
            },
            replace: true,
            restrict: "E",
            templateUrl: "app/components/dialogs/ui/iriList/iriList.html",
            link: function ($scope, element, attrs, ngModel) {

                if (!ngModel) {
                    console.log("ngModel is not set!");
                    return;
                }

                /**
                 * Update on change of value in the primary field.
                 */
                $scope.onChange = function () {
                    ngModel.$setViewValue($scope.data);
                };

                /**
                 * Propagate changed from outside.
                 */
                ngModel.$render = function () {
                    const modelValue = ngModel.$modelValue;
                    if (modelValue === undefined || modelValue === null) {
                        $scope.data = [];
                    } else if (Array.isArray(modelValue)) {
                        $scope.data = modelValue;
                    } else {
                        $scope.data = [{"@id": modelValue}];
                    }
                };

                $scope.onAdd = function (index) {
                    $scope.data.splice(index + 1, 0, {"@id": ""});
                    $scope.onChange();
                };

                $scope.onDelete = function (index) {
                    $scope.data.splice(index, 1);
                    $scope.onChange();
                };

                ngModel.$render();
            }
        };
    }

    let isInitialized = false;
    return function init(app) {
        if (isInitialized) {
            return;
        } else {
            isInitialized = true;
        }
        app.directive("lpIriList", directive);
    };
});
