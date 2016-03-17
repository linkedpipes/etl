/**
 * Directorive for list of IRI.
 */
define([], function () {

    function directive() {
        return {
            require: 'ngModel',
            scope: {
                'topLabel': '@labelTop',
                'itemLabel': '@labelItem'
            },
            replace: true,
            restrict: 'E',
            templateUrl: 'app/components/inputs/iriList/iriList.html',
            link: function ($scope, element, attrs, ngModel) {

                $scope.required = attrs.required;

                if (!ngModel) {
                    console.log('ngModel is not set!');
                    return;
                }

                /**
                 * Update on change of value in the primary field.
                 */
                $scope.onChange = function () {
                    ngModel.$setViewValue($scope.data);
                    console.log('set', $scope.data);
                };

                /**
                 * Propagate changed from outside.
                 */
                ngModel.$render = function () {
                    if ($.isArray(ngModel.$modelValue)) {
                        $scope.data = ngModel.$modelValue;
                    } else {
                        console.log('Empty init');
                        $scope.data = [{'@id': ngModel.$modelValue}];
                    }
                };

                $scope.onAdd = function (index) {
                    $scope.data.splice(index + 1, 0, {'@id': ''});
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
        app.directive('lpIriList', directive);
    }
    return init;
});
