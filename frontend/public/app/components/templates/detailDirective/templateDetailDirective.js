define([
    "jquery", "jsonld",
    "../generalTab/templateGeneralTab",
    "../hierarchyTab/templateHierarchyTab",
    "../embedDirective/templateEmbedDirective",
    "../templateService",
    "../../dialogs/dialogService"
], function (jQuery, jsonld, generalTab, hierarchyTab, embedDirective,
             templateService, templateDialogService) {
    "use strict";

    const PARENT_PATH = "app/components/templates/detailDirective/";

    function directive($rootScope, templateService, templateDialogService) {

        function controller($scope) {

            // Instance used to communicate with the dialog.
            $scope.dialogService = templateDialogService.new();

            // Scope used by the dialog.
            $scope.dialogScope = $rootScope.$new(true);

            // Services to bind to the dialog.
            $scope.dialogLocals = {
                "$service": $scope.dialogService
            };

            /**
             * Set all working variables.
             */
            function load() {
                const template = $scope.api.store.template;
                const templateToEdit = $scope.api.store.templateToEdit;
                const configuration = $scope.api.store.configuration;

                $scope.template = template;
                $scope.dialogService.api.setIri(template.id);
                $scope.templateToEdit = templateToEdit;
                $scope.dialogService.api.setInstanceConfig(configuration);
                //
                const parent = template.template;
                if (parent === undefined) {
                    $scope.dialogService.api.setTemplateConfig(undefined);
                    // Load dialogs.
                    $scope.dialogs = templateService.getDialogs(
                        template.id, true);
                    return;
                }
                // Load parent configuration.
                templateService.fetchEffectiveConfig(parent).then((config) => {
                    // For sure we use new object here.
                    $scope.dialogService.api.setTemplateConfig(
                        jQuery.extend(true, [], config))
                    // Load dialogs.
                    $scope.dialogs = templateService.getDialogs(
                        template.id, true);
                });
            }

            /**
             * Make sure that the content in the shared objects is
             * updated.
             */
            $scope.api.save = () => {
                if ($scope.dialogService.onStore !== undefined) {
                    $scope.dialogService.onStore();
                }
            };

            $scope.api.load = load;

            // The data might be already ready, as we do not know
            // if the parent or we get to run first.
            if ($scope.api.store) {
                load();
            }
        }

        return {
            "restrict": "E",
            "templateUrl": PARENT_PATH + "templateDetailDirective.html",
            "scope": {
                // Shared API object.
                // Use store property to transfer data.
                "api": "="
            },
            "controller": controller
        };
    }

    var _initialized = false;
    return function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        //
        generalTab(app);
        hierarchyTab(app);
        embedDirective(app);
        templateService(app);
        templateDialogService(app);
        //
        app.directive("lpTemplateDetail", ["$rootScope",
            "template.service", "template.dialog.service", directive]);
    };

});
