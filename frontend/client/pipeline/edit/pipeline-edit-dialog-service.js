((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "./mode/execution/detail-dialog/component-execution-ctrl"
    ], definition);
  }
})((_executionDetailDialog) => {
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
        "template": require("@client/template/select-dialog/template-select-dialog.html"),
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
        "template": require("@client/component/detail-dialog/instance-detail-dialog.html"),
        "parent": angular.element(document.body),
        "clickOutsideToClose": false,
        "fullscreen": useFullScreen(),
        "locals": {
          "component": component,
          "component-template": template,
          "configuration": configuration
        },
        "escapeToClose": false
      });
    };

    service.selectPipeline = () => {
      return $mdDialog.show({
        "controller": "components.pipelines.import.dialog",
        "template": require("../import/pipeline-import-dialog-view.html"),
        "parent": angular.element(document.body),
        "hasBackdrop": false,
        "clickOutsideToClose": true,
        "fullscreen": useFullScreen()
      });
    };

    service.createTemplate = (component, template, configuration) => {
      return $mdDialog.show({
        "controller": "template.detail.dialog",
        "template": require("./template-detail-dialog/template-detail-dialog.html"),
        "clickOutsideToClose": false,
        "fullscreen": useFullScreen(),
        "locals": {
          "component": component,
          "component-template": template,
          "configuration": configuration
        }
      });
    };

    service.debugDetail = (
      component, componentExecution, execution) => {
      return $mdDialog.show({
        "controller": "components.component.execution.dialog",
        "template": require("./mode/execution/detail-dialog/component-execution-view.html"),
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
        "template": require("./detail-dialog/pipeline-detail-dialog-view.html"),
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
    _executionDetailDialog(app);
    app.factory("components.pipeline.canvas.dialogs", factory);
  }

});
