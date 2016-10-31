
/**
 * Directive that embeds controller and HTML template on given URIs
 * and display them.
 */
"use strict";
define([], function () {

    function directive($templateRequest, $controller, $compile) {

        function link($scope, element, attrs) {
            require([$scope.js], function () {
                // We require exactly one module.
                if (arguments.length !== 1) {
                    return;
                }
                const controller = arguments[0];
                // Load the HTML.
                $templateRequest($scope.html).then((htmlTemplate) => {
                    // Create a DOM from given template.
                    const templateElement = angular.element(htmlTemplate);
                    // Instantiate a controller passing
                    // given variables (locals to bind). We have
                    // to make sure that the content is one
                    // of those variables to bind.
                    $scope.locals['$scope'] = $scope.scope;
                    // The arguments[0] contains function as returned
                    // by the required module.
                    $controller(controller, $scope.locals);
                    // Compile the element to the template function.
                    const templateFunction = $compile(templateElement);
                    // Bind controller to the template.
                    const modalDomEl = templateFunction($scope.scope);
                    // Add new HTML to the DOM.
                    element.append(modalDomEl);
                    // Now when the component is loaded
                    // we can store the configuration.

                    // TODO Notify called that component is ready !

                    // TODO We can cache the templateFunction ?

                });
            });
        }

        return {
            'restrict': 'E',
            'scope': {
                'js': '=',
                'html': '=',
                'scope': '=',
                'locals': '='
            },
            'template': '<div></div>',
            'link': link
        };
    }

    return (app) => {
        app.directive('lpComponentReference', ['$templateRequest',
            '$controller', '$compile', directive]);
    }

})
