/**
 *
 * Directive that embeed controler and HTML tempalte on given URIs and present them. The given parametr
 * configurationWrap is passed to the component.
 */
define([], function () {
    function directive($templateRequest, $rootScope, $controller, $compile, $document) {
        return {
            restrict: 'E',
            scope: {
                api: '=',
                uri: '=',
                uriHtml: '='
            },
            template: '<div id="configurationHolderPlaceHolder"></div>',
            link: function ($scope, element, attrs) {
                $scope.childScope = $rootScope.$new();
                require([$scope.uri], function () {
                    // We may get more modules.
                    if (arguments.length === 1) {
                        var module = arguments[0];
                        //
                        var promise = $templateRequest($scope.uriHtml);
                        promise.then(function (htmlTemplate) {
                            // Create template from downloaded HTML.
                            var angularDomEl = angular.element('<div></div>');
                            angularDomEl.html(htmlTemplate);
                            // Register controller.
                            $controller(module, {
                                '$scope': $scope.childScope,
                                'configurationWrap': $scope.configurationWrap
                            });
                            // Bond controller to HTML.
                            var modalDomEl = $compile(angularDomEl)($scope.childScope);
                            // Add to the HTMl DOM.
                            var body = $document.find('#configurationHolderPlaceHolder');
                            body.append(modalDomEl);
                            // Load configuration ~ configuration can be set before the component is ready
                            // in such case it's stored in scope.configuration.
                            if ($scope.configuration) {
                                $scope.childScope.setConfiguration($scope.configuration);
                                // Unset configuration.
                                delete $scope.configuration;
                            }
                        });
                    }
                });
            },
            controller: ['$scope', function ($scope) {
                    $scope.api.setConfiguration = function (configuration) {
                        if ($scope.childScope.setConfiguration) {
                            $scope.childScope.setConfiguration(configuration);
                        } else {
                            // Not initialized yet, store - will be set once the component is initialized.
                            $scope.configuration = configuration;
                        }
                    };
                    /**
                     *
                     * @returns Configuration JSON-LD object, or nothing if configuration component has not yet been loaded.
                     */
                    $scope.api.getConfiguration = function () {
                        if ($scope.childScope.getConfiguration) {
                            return $scope.childScope.getConfiguration();
                        } else {
                            // Not ready, return nothing.
                        }
                    };
                    $scope.api.isValid = function () {
                        if ($scope.childScope.isValid) {
                            return $scope.childScope.isValid();
                        } else {
                            return true;
                        }
                    };
                }]
        };
    }
    //
    function init(app) {
        app.directive('configurationHolder',
                ['$templateRequest', '$rootScope', '$controller', '$compile', '$document', directive]);
    }
    return init;
});