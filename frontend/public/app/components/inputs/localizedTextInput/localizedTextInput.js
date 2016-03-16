/**
 * Directorive for a text label with support for multiple languages in
 * JSON-LD form.
 */
define([], function () {

    function directive() {
        return {
            require: 'ngModel',
            scope: {
                'label': '@label'
            },
            replace: true,
            restrict: 'E',
            templateUrl: 'app/components/inputs/localizedTextInput/localizedTextInput.html',
            link: function ($scope, element, attrs, ngModel) {

                if (!ngModel) {
                    console.log('ngModel is not set!');
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
                    console.log('render');
                    if ($.isArray(ngModel.$modelValue)) {
                        $scope.data = ngModel.$modelValue;
                    } else {
                        $scope.data = [{
                                '@language': 'en',
                                '@value': ngModel.$modelValue
                            }];
                    }
                };

                $scope.onAdd = function () {
                    $scope.data.push({
                        '@language': '',
                        '@value': ''
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
    //
    function init(app) {
        app.directive('lpLocalizedTextInput', directive);
    }
    return init;
});
