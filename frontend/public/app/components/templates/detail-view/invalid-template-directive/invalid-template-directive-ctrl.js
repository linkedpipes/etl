define([
    "../../generalTab/templateGeneralTab",
    "../../hierarchyTab/templateHierarchyTab",
    "../../usageTab/templateUsageTab",
    "../../template-service",
], function (_generalTab, _hierarchyTab, _usageTab, _templateService) {
    "use strict";

    function directive($location, $status, $templates) {

        function controller($scope) {
            $scope.isUsed = true;

            function load() {
                const template = $scope.api.store.template;
                const templateToEdit = $scope.api.store.templateToEdit;

                $scope.template = template;

                $scope.templateToEdit = templateToEdit;
                const parent = template.template;
                if (parent === undefined) {
                    $scope.dialogService.api.setTemplateConfig(undefined);
                    // Load dialogs.
                    $scope.dialogs = $templates.getDialogs(
                        template.id, true);
                    return;
                }
                // Load usage.
                $templates.getUsage(template.id).then((usage) => {
                    $scope.usage = usage;
                    $scope.isUsed = Object.keys(usage).length !== 0;
                });
            }

            $scope.onDelete = () => {
                $templates.delete($scope.template.id).then(() => {
                    $status.success("Template deleted.");
                    $location.path("/templates").search({});
                }, (response) => {
                    $status.httpError("Can't delete the template.", response);
                });
            };

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

        const templateUrl = "app/components/templates/detail-view/" +
            "invalid-template-directive/invalid-template-directive-view.html";
        return {
            "restrict": "E",
            "templateUrl": templateUrl,
            "scope": {
                // Shared API object - used to transfer data.
                "api": "=",
                "showTemplateManagement": "=",
                "enableTemplateLink": "="
            },
            "controller": controller
        };
    }

    let _initialized = false;
    return function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        //
        _generalTab(app);
        _hierarchyTab(app);
        _usageTab(app);
        _templateService(app);
        //
        app.directive("lpInvalidTemplateDetail", [
            "$location", "services.status", "template.service",
            directive]);
    };

});
