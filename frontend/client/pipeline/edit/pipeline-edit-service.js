((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "./canvas/canvas-service",
      "./edit-mode/canvas-edit-mode-service",
      "./execution-mode/canvas-execution-mode-service",
      "@client/template/select-dialog/template-select-dialog",
      "./detail-dialog/pipeline-detail-dialog-ctrl",
      "../../component/detail-dialog/instance-detail-dialog",
      "../import/pipeline-import-dialog-ctrl",
      "./template-detail-dialog/template-detail-dialog",
      "./pipeline-edit-dialog-service",
      "../pipeline-api",
      "./pipeline-actions",
      "./canvas/pipeline-loader",
      "../../execution/execution-api",
      "@client/execution/execution-status-to-icon",
      "file-saver",
    ], definition);
  }
})((_canvasService, _editModeService, _executionModeService,
    _templateSelectDialog, _pipelineDetailDialog, _componentDetailDialog,
    _pipelineImportDialog, _templateDialog, _dialogService,
    pipelines, actions, loader, executions, statusToIcon, saveAs) => {
  "use strict";

  function factory(
    $location, $http, $mdDialog, $mdMedia,
    $pageLayout, $templates, $status, $refresh,
    $canvas, $editMode, $executionMode, $dialogs) {

    let $scope;
    let data = {
      "pipelineIri": undefined,
      "executionIri": undefined,
      "executionUpdate": false
    };

    let actionsCallbacks = {
      "onStartExecution": (iri) => {
        // Called when new execution is started.
        activateExecutionMode();
        data.executionIri = iri;
        loadExecution();
      }
    };

    function initialize(scope, pipeline, execution) {
      $scope = scope;
      $scope.canvasApi = {};
      $scope.isEditMode = true;
      $scope.pipelineLabel = "";
      $scope.isExecutionFinished = false;
      $scope.executionIcon = {
        "name": "",
        "style": {}
      };
      //
      data["pipelineIri"] = pipeline;
      data["executionIri"] = execution;
    }

    function onHtmlReady() {
      actions.bind(
        $canvas, $templates, $dialogs, $status, $http, $location,
        actionsCallbacks);
      $canvas.bindUserApi(actions);
      $editMode.bind($canvas, actions);
      $executionMode.bind($canvas, actions);
      if (data["executionIri"]) {
        activateExecutionMode();
      } else {
        activateEditMode();
      }
      loadDataForFirstTime();
    }

    function activateEditMode() {
      $scope.isEditMode = true;
      $canvas.setInteractive(true);
      //
      $editMode.setEnabled(true);
      $executionMode.setEnabled(false);
      //
      $pageLayout.color = "#2196F3";
    }

    function activateExecutionMode() {
      $scope.isEditMode = false;
      $canvas.setInteractive(false);
      //
      $editMode.setEnabled(false);
      $executionMode.setEnabled(true);
      //
      $pageLayout.color = "#FF9800";
    }

    function loadDataForFirstTime() {
      console.time("Loading pipeline");
      $templates.load()
        .then(() => pipelines.loadLocal($http, data.pipelineIri))
        .then((jsonld) => {
          const pipeline = actions.pipelineFromJsonLd(jsonld);
          updatePipelineLabel();
          const report = loader.loadPipeline($canvas, pipeline);
          // TODO Use loading report!
          report.then((reportMessage) => {
            console.log("Loading report:", reportMessage);
          });
        })
        .catch((error) => {
          $status.error("Can't load pipeline", error);
        })
        .then(() => console.timeEnd("Loading pipeline"))
        .then(loadExecution)
        .catch((error) => {
          $status.error("Can't load execution", error);
        })
    }

    function loadExecution() {
      if (!data.executionIri) {
        return;
      }
      executions.loadLocal($http, data.executionIri)
        .then((jsonld) => {
          updateExecutionFromJsonld(jsonld);
          if (!$scope.isExecutionFinished) {
            data.executionUpdate = true;
            // As the refresher remove all listeners after
            // navigation, it's fine to call it here.
            // loadExecution is called only when new execution is
            // set. So either on the first load of the page,
            // or after a navigation.
            $refresh.add("ppl-canvas", updateExecution);
          }
        });
    }

    function updateExecutionFromJsonld(jsonld) {
      actions.executionFromJsonLd(jsonld, data.executionIri);
      $scope.isExecutionFinished = actions.isExecutionFinished();
      $scope.executionIcon =
        statusToIcon(actions.getExecutionStatus(), true);
    }

    // Replacing execution cause reload and redraw of everything.
    // TODO Do not replace execution, instead just update it.
    function updateExecution() {
      if (!data.executionUpdate) {
        return;
      }
      executions.loadLocal($http, data.executionIri)
        .then((jsonld) => {
          updateExecutionFromJsonld(jsonld);
          if ($scope.isExecutionFinished) {
            data.executionUpdate = false;
          }
        });
    }

    function updatePipelineLabel() {
      let label = actions.getPipelineLabel();
      if (label === undefined || label === "") {
        label = actions.getPipelineIri();
      }
      $scope.pipelineLabel = label;
    }

    function onClose() {
      if (data.executionIri === undefined) {
        $location.path("/pipelines").search({});
      } else {
        $location.path("/executions").search({});
      }
    }

    function onEditMode() {
      activateEditMode();
    }

    function onSave() {
      actions.savePipeline().catch(onCantSave);
    }

    function onCantSave(error) {
      $status.error("Can't save pipeline.", error);
    }

    function onDelete() {
      const dialog = $dialogs.deleteConfirmationDialog();
      $mdDialog.show(dialog).then(() => {
        pipelines.deletePipeline($http, actions.getPipelineIri())
          .then(onClose)
          .catch((error) => {
            $status.error("Can't delete the pipeline.", error);
          })
      });
    }

    function onPipelineDetail() {
      const definition = actions.getPipelineResource();
      const profile = actions.getExecutionProfile();
      $dialogs.onPipelineDetail(definition, profile)
        .then(updatePipelineLabel);
    }

    function onBackupDownload() {
      $canvas.synchronize();
      const pipeline = actions.asJsonLd();
      saveAs(createPipelineBlob(pipeline),
        $scope.pipelineLabel + ".jsonld");
    }

    function createPipelineBlob(json) {
      const data = JSON.stringify(json, null, 2);
      return new Blob([data], {"type": "text/json"})
    }

    function onDownload() {
      const url = actions.getPipelineIri() +
        "?templates=true" +
        "&mappings=true" +
        "&removePrivateConfig=false";
      actions.savePipeline().then(() => {
        $http.get(url).then((response) => {
          saveAs(createPipelineBlob(response.data),
            $scope.pipelineLabel + ".jsonld");
        });
      });
    }

    function onDownloadNoCredentials() {
      const url = actions.getPipelineIri() +
        "?templates=true" +
        "&mappings=true" +
        "&removePrivateConfig=true";
      actions.savePipeline().then(() => {
        $http.get(url).then((response) => {
          saveAs(createPipelineBlob(response.data),
            $scope.pipelineLabel + ".jsonld");
        });
      });
    }

    function onCopyPipeline() {
      $canvas.synchronize();
      const pipeline = actions.asJsonLd();
      const label = "Copy of " + $scope.pipelineLabel;
      pipelines.createPipelineFromData($http, pipeline, label)
        .then((iri) => {
          $status.success("Pipeline has been successfully copied.");
          $location.path("/pipelines/edit/canvas")
            .search({"pipeline": iri});
        })
        .catch(() => {
          $status.error("Can't create new pipeline.", error)
        });
    }

    function onCantExecute(error) {
      $status.error("Can't start the execution.", error);
    }

    function onExecute() {
      actions.savePipeline().then(() => {
        actions.executePipeline({
          "keepDebugData": true
        }).catch(onCantExecute);
      }).catch(onCantSave);
    }

    function onExecuteWithoutDebugData() {
      actions.savePipeline().then(() => {
        actions.executePipeline({
          "keepDebugData": false
        }).catch(onCantExecute);
      }).catch(onCantSave);
    }

    function onDownloadNoSave() {
      const url = actions.getPipelineIri() +
        "?templates=true" +
        "&mappings=true" +
        "&removePrivateConfig=false";
      $http.get(url).then((response) => {
        saveAs(createPipelineBlob(response.data),
          $scope.pipelineLabel + ".jsonld");
      });
    }

    function onDownloadNoCredentialsNoSave() {
      const url = actions.getPipelineIri() +
        "?templates=true" +
        "&mappings=true" +
        "&removePrivateConfig=true";
      $http.get(url).then((response) => {
        saveAs(createPipelineBlob(response.data),
          $scope.pipelineLabel + ".jsonld");
      });
    }

    function onCopyPipelineNoSave() {
      pipelines.copy($http, {"iri": data.pipelineIri})
        .then(onCopyPipelineSuccess)
        .catch(() => {
          $status.error("Can't create new pipeline.", error)
        });
    }

    function onCopyPipelineSuccess(response) {
      const jsonld = response.data;
      const iri = jsonld[0]["@graph"][0]["@id"];
      $status.success("Pipeline has been successfully copied.");
      $location.path("/pipelines/edit/canvas")
        .search({"pipeline": iri});
    }

    return {
      "initialize": initialize,
      "onHtmlReady": onHtmlReady,
      "onClose": onClose,
      "onEditMode": onEditMode,
      "onSave": onSave,
      "onDelete": onDelete,
      "onPipelineDetail": onPipelineDetail,
      "onBackupDownload": onBackupDownload,
      "onDownload": onDownload,
      "onDownloadNoCredentials": onDownloadNoCredentials,
      "onCopyPipeline": onCopyPipeline,
      "onExecute": onExecute,
      "onExecuteWithoutDebugData": onExecuteWithoutDebugData,
      "onDisableAllLoaders": actions.onDisableAllLoaders,
      "onEnableAllLoaders": actions.onEnableAllLoaders,
      "onDownloadNoSave": onDownloadNoSave,
      "onDownloadNoCredentialsNoSave": onDownloadNoCredentialsNoSave,
      "onCopyNoSave": onCopyPipelineNoSave
    };
  }

  factory.$inject = [
    "$location", "$http", "$mdDialog", "$mdMedia",
    "app-layout.service",
    "template.service",
    "status",
    "refresh",
    "canvas.service",
    "canvas.edit-mode.service",
    "canvas.execution-mode.service",
    "components.pipeline.canvas.dialogs"
  ];

  let initialized = false;
  return function init(app) {
    if (initialized) {
      return;
    }
    initialized = true;
    _canvasService(app);
    _editModeService(app);
    _executionModeService(app);
    _templateSelectDialog(app);
    _pipelineDetailDialog(app);
    _componentDetailDialog(app);
    _pipelineImportDialog(app);
    _templateDialog(app);
    _dialogService(app);
    app.factory("components.pipeline.canvas.service", factory);
  }

})
