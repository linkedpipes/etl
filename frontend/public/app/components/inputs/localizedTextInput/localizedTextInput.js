/**
 * Directorive for a text label with support for multiple languages in
 * JSON-LD form.
 *
 * Dialog usage example:
 *
 *   $scope.dialog.content = rdf.getValue(resource, 'content');
 *
 *   rdf.setValue(resource, 'content', $scope.dialog.content);
 *
 * Java usage example:
 *
 *  @RdfToPojo.Type(uri = ")
 *  public class Configuration {
 *
 *   @RdfToPojo.Value
 *   public static class LocalizedString {
 *
 *     @RdfToPojo.Value
 *     private String value;
 *
 *     @RdfToPojo.Lang
 *     private String language;
 *
 *   }
 *
 *   @RdfToPojo.Property(uri = "http://www.w3.org/2004/02/skos/core#prefLabel")
 *   private List<LocalizedString> label = new LinkedList<>();
 *
 *  }
 *
 *  Appropriete getters and setters must be provided for all the properties.
 *
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
                    if ($.isArray(ngModel.$modelValue)) {
                        $scope.data = ngModel.$modelValue;
                    } else {
                        $scope.data = [{
                                '@language': 'en',
                                '@value': ngModel.$modelValue
                            }];
                    }
                };

                $scope.onAdd = function (index) {
                    $scope.data.splice(index + 1, 0, {
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
