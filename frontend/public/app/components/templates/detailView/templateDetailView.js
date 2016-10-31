define([
    "jquery",
    "../detailDirective/templateDetailDirective",
    "../templateService"
], function (jQuery, detailDirective, templateService) {
    "use strict";

    function controller($scope, $routeParams, statusService, templateService) {

        $scope.api = {};

        $scope.templateToEdit = undefined;
        $scope.configuration = undefined;

        $scope.onSave = () => {
            // Update shared data.
            $scope.api.save();

            templateService.saveTemplate($scope.templateToEdit,
                $scope.configuration).then(() => {
                statusService.success({
                    "title": "Template saved."
                });
            }, () => {
                statusService.error({
                    "title": "Can't save template."
                });
            });
        };

        (function init() {
            templateService.load().then(() => {
                const template = templateService.getTemplate(
                    $routeParams.template);

                $scope.templateToEdit = templateService.getEditableTemplate(
                    $routeParams.template);

                templateService.fetchConfig(template.id).then(
                    (instance) => {
                        $scope.configuration =
                            jQuery.extend(true, [], instance);
                        // Pass data to the directive.
                        $scope.api.store = {
                            "template" : template,
                            "templateToEdit": $scope.templateToEdit,
                            "configuration" : $scope.configuration
                        };
                        if ($scope.api.load !== undefined) {
                            $scope.api.load();
                        }
                    });
            });
        })();
    }

    var _initialized = false;
    return function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        //
        detailDirective(app);
        templateService(app);
        //
        app.controller("template.detail", ["$scope", "$routeParams",
            "services.status", "template.service", controller]);
    };

});
