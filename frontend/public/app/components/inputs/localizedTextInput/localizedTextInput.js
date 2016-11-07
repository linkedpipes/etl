/**
 * Directive for a text label with support for multiple languages.
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
                "disabled": "=lpDisabled",
                "multiline" : "@multiline"
            },
            replace: true,
            restrict: "E",
            templateUrl: "app/components/inputs/localizedTextInput/localizedTextInput.html",
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
                    if (jQuery.isArray(ngModel.$modelValue)) {
                        $scope.data = ngModel.$modelValue;
                    } else {
                        $scope.data = [{
                            "@language": "en",
                            "@value": ngModel.$modelValue
                        }];
                    }
                };

                $scope.onAdd = function (index) {
                    $scope.data.splice(index + 1, 0, {
                        "@language": "",
                        "@value": ""
                    });
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
        app.directive("lpLocalizedTextInput", directive);
    };
});
