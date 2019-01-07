((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    }
})(() => {
    "use strict";

    function factory($mdDialog, $mdMedia) {

        const service = {};

        service.deleteConfirmationDialog = () => {
            return $mdDialog.confirm()
                .title("Would you like to delete this pipeline?")
                .ariaLabel("Delete pipeline.")
                .targetEvent(event)
                .ok("Delete pipeline")
                .cancel("Cancel");
        };

        service.selectNewTemplate = (templateIri, port) => {
            const filter = {};
            if (templateIri) {
                filter["templateIri"] = templateIri;
                filter["binding"] = port;
            }
            return $mdDialog.show({
                "controller": "components.templates.select.dialog",
                "templateUrl": "app/components/templates/selectDialog/templateSelectDialog.html",
                "parent": angular.element(document.body),
                "hasBackdrop": false,
                "clickOutsideToClose": true,
                "fullscreen": useFullScreen(),
                "locals": {
                    "filter": filter
                }
            });
        };

        function useFullScreen() {
            return $mdMedia("sm") || $mdMedia("xs");
        }

        service.editComponent = (component, template, configuration) => {
            return $mdDialog.show({
                "controller": "instance.detail.dialog",
                "templateUrl": "app/components/dialogs/detailDialog/instanceDetailDialog.html",
                "parent": angular.element(document.body),
                "clickOutsideToClose": false,
                "fullscreen": useFullScreen(),
                "locals": {
                    'component': component,
                    'template': template,
                    'configuration': configuration
                },
                'escapeToClose': false
            });
        };

        service.selectPipeline = () => {
            return $mdDialog.show({
                "controller": "components.pipelines.import.dialog",
                "templateUrl": "app/components/pipelines/import-dialog/pipeline-import-dialog-view.html",
                "parent": angular.element(document.body),
                "hasBackdrop": false,
                "clickOutsideToClose": true,
                "fullscreen": useFullScreen()
            });
        };

        service.createTemplate = (component, template, configuration) => {
            return $mdDialog.show({
                "controller": "template.detail.dialog",
                "templateUrl": "app/components/pipelines/edit-view/template-detail-dialog/template-detail-dialog.html",
                "clickOutsideToClose": false,
                "fullscreen": useFullScreen(),
                "locals": {
                    "component": component,
                    "template": template,
                    "configuration": configuration
                }
            });
        };

        service.debugDetail = (
            component, componentExecution, execution) => {
            return $mdDialog.show({
                "controller": "components.component.execution.dialog",
                "templateUrl": "app/components/pipelines/edit-view/execution-mode/detail-dialog/component-execution-view.html",
                "hasBackdrop": false,
                "clickOutsideToClose": true,
                "fullscreen": false,
                "locals": {
                    "component": component,
                    "execution": componentExecution,
                    "executionIri": execution
                }
            });
        };

        service.noDebugDataDialog = () => {
            console.log("noDebugDataDialog");
            const dialog = $mdDialog.alert()
                .parent(angular.element(document.querySelector("body")))
                .clickOutsideToClose(true)
                .title("There is no debug data.")
                .textContent("")
                .ok("Ok");
            $mdDialog.show(dialog);
        };

        service.onPipelineDetail = (definition, profile) => {
            return $mdDialog.show({
                "controller": "components.pipelines.detail.dialog",
                "templateUrl": "app/components/pipelines/edit-view/detail-dialog/pipeline-detail-dialog-view.html",
                "parent": angular.element(document.body),
                "clickOutsideToClose": false,
                "fullscreen": useFullScreen(),
                "locals": {
                    "data": {
                        "definition": definition,
                        "profile": profile
                    }
                }
            })
        };

        return service;
    }

    factory.$inject = ["$mdDialog", "$mdMedia"];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        app.factory("components.pipeline.canvas.dialogs", factory);
    }

});
