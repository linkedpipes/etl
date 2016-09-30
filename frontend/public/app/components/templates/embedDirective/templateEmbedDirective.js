/**
 * Directive for embedding controller and HTML template.
 */

define([], function () {
    "use strict";

    function directive($templateRequest, $controller, $compile) {

        function link(scope, element) {
            require([scope.js], function () {
                // We require exactly one module.
                if (arguments.length !== 1) {
                    // ERROR LOG
                    return;
                }
                const controller = arguments[0];
                // Load the HTML.
                $templateRequest(scope.html).then((htmlTemplate) => {
                    // Create a DOM from given template.
                    // console.time('lpTemplateDialog.element')
                    const templateElement = angular.element(htmlTemplate);
                    // console.timeEnd('lpTemplateDialog.element')
                    // Instantiate a controller and pass given variables
                    // (locals to bind). We have to make sure that the
                    // content is one of those variables to bind.
                    scope.locals.$scope = scope.scope;
                    $controller(controller, scope.locals);
                    // Compile the element to the template function.
                    // console.time('lpTemplateDialog.compile')
                    const templateFunction = $compile(templateElement);
                    // console.timeEnd('lpTemplateDialog.compile')
                    // Bind controller to the template.
                    // console.time('lpTemplateDialog.bind')
                    const modalDomEl = templateFunction(scope.scope);
                    // console.timeEnd('lpTemplateDialog.bind')
                    // Add new HTML to the DOM.
                    // console.time('lpTemplateDialog.add')
                    element.append(modalDomEl);
                    // console.timeEnd('lpTemplateDialog.add');
                    if (scope.onLoad) {
                        scope.onLoad();
                    }
                });
            });
        }

        return {
            'restrict': 'A',
            'scope': {
                // Path to the JS script file.
                'js': '=',
                // Path to the HTML file.
                'html': '=',
                // Scope given to the embedded component.
                'scope': '=',
                // Map of additional properties to pass the embedded component
                'locals': '=',
                // Callback.
                'onLoad': '&'
            },
            'link': link
        };
    }

    var _initialized = false;
    return function init (app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        app.directive('lpEmbed', ['$templateRequest',
            '$controller', '$compile', directive]);
    };

})
