define([
    "jquery",
    "../../templates/generalTab/templateGeneralTab",
    "../../templates/embedDirective/templateEmbedDirective",
    "../../templates/templateService",
    "../../dialogs/dialogService",
    "../hierarchyTab/instanceHierarchyTab"
], function (jQuery, generalTab, embedDirective,
             templateService, templateDialogService, hierarchyTab) {
    "use strict";

    const PARENT_PATH = "app/components/instances/detailDirective/";

    function directive($rootScope, templateService, templateDialogService) {

        function controller($scope) {

            // Instance used to communicate with the dialog.
            $scope.dialogService = templateDialogService.new();

            // Scope used by the dialog.
            $scope.dialogScope = $rootScope.$new(true);
            $scope.dialogScope.application = {
                "changeToHierarchyTab": changeToHierarchyTab
            };

            // Services to bind to the dialog.
            $scope.dialogLocals = {
                "$service": $scope.dialogService
            };

            /**
             * Set all working variables.
             */
            function load() {

                // const template = $scope.api.store.template;
                const instance = $scope.api.store.instance;
                const parent = $scope.api.store.parent;
                const configuration = $scope.api.store.configuration;

                $scope.dialogService.api.setIri(instance.id);
                $scope.instance = instance;
                $scope.parent = parent;
                $scope.dialogService.api.setInstanceConfig(configuration);
                // Load parent configuration.
                templateService.fetchEffectiveConfig(parent.id).then((config) => {
                    // For sure we use new object here.
                    $scope.dialogService.api.setTemplateConfig(
                        jQuery.extend(true, [], config))
                    // Load dialogs.
                    $scope.dialogs = templateService.getDialogs(
                        parent.id, false);
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

            function changeToHierarchyTab() {
                $scope.activeTab = $scope.dialogs.length + 1;
            }

        }

        return {
            "restrict": "E",
            "templateUrl": PARENT_PATH + "instanceDetailDirective.html",
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
        embedDirective(app);
        templateService(app);
        templateDialogService(app);
        hierarchyTab(app);
        //
        app.directive("lpInstanceDetail", ["$rootScope",
            "template.service", "template.dialog.service", directive]);
    };

});
