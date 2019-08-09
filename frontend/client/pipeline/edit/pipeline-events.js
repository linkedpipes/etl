((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "jquery",
      "@client/app-service/jsonld/jsonld",
      "./model/pipeline-model",
      "./model/execution-model",
      "./canvas/pipeline-loader"
    ], definition);
  }
})((jQuery, jsonld, pplModel, execModel, loader) => {

  let $pipeline;

  let $execution;

  let $canvasService;

  let $templatesService;

  let $dialogsService;

  let $statusService;

  let $actions;

  function onPositionChange(resource, x, y) {
    pplModel.component.setPosition(resource, x, y);
  }

  function onSourceChange(connection, source, port) {
    const oldSource = pplModel.connection.getSource(connection);
    const oldPort = pplModel.connection.getSourceBinding(connection);
    if (oldSource === jsonld.r.getId(source) && oldPort === port) {
      return;
    }
    // Invalidate mapping.
    const target = pplModel.connection.getTarget(connection);
    componentChanged(target);
    // Update model.
    pplModel.connection.setSource(connection, source, port);
  }

  function onTargetChange(connection, target, port) {
    const oldTarget = pplModel.connection.getTarget(connection);
    const oldPort = pplModel.connection.getTargetBinding(connection);
    if (oldTarget === jsonld.r.getId(target) && oldPort === port) {
      return;
    }
    // Invalidate mapping - both old and new target.
    componentChanged(oldTarget);
    componentChanged(jsonld.r.getId(target));
    // Update model.
    pplModel.connection.setTarget(connection, target, port);
  }

  function onConnectionCreate(type, source, sPort, target, tPort) {
    let connection;
    if (type === "link") {
      connection = pplModel.createDataLink($pipeline);
    } else {
      connection = pplModel.createRunAfterLink($pipeline);
    }
    pplModel.connection.setSource(connection, source, sPort);
    pplModel.connection.setTarget(connection, target, tPort);
    // Invalidate mapping.
    componentChanged(jsonld.r.getId(target));
    //
    return connection;
  }

  function onDelete(resource) {
    // We need to check if the resource is a connection or a component.
    if (pplModel.isDataLink(resource)) {
      const target = pplModel.connection.getTarget(resource);
      componentChanged(target);
    } else if (pplModel.isComponent(resource)) {
      componentChanged(jsonld.r.getId(resource));
    }
    //
    pplModel.deleteByIri($pipeline, jsonld.r.getId(resource));
  }

  function onComponentDetail(cell, component) {
    // TODO Search for use and replace with getComponentTemplate.
    const templateIri = pplModel.component.getTemplateIri(component);
    const template = $actions.getTemplate(templateIri);
    const configuration = $actions.getComponentConfiguration(component);
    $dialogsService.editComponent(component, template, configuration)
      .then((response) => {
        if (!response.saved) {
          return;
        }
        // Update resource with component information (name, ...)
        pplModel.setResource($pipeline, response.component);
        $actions.setComponentConfiguration(
          response.component, response.configuration);
        // Update visual and mark component as changed.
        const componentIri = jsonld.r.getId(component);
        $canvasService.updateComponent(componentIri);
        componentChanged(componentIri);
      });
  }

  function componentChanged(iri) {
    const execComponent = execModel.getComponent($execution, iri);
    if (execComponent === undefined) {
      // Component is not part of the execution, we do not
      // propagate behind that point.
      return;
    }
    if (execModel.isChanged($execution, iri)) {
      return;
    }
    execModel.onChange($execution, execComponent);
    // We need to update visual as we"ve  may changed the mapping.
    $canvasService.updateComponent(iri);
    // Propagation - we need to disable all following.
    const connections = pplModel.getDataLinks($pipeline);
    const conService = pplModel.connection;
    connections.forEach((connection) => {
      if (conService.getSource(connection) === iri) {
        componentChanged(conService.getTarget(connection));
      }
    });
  }

  function onDeleteComponent(component) {
    componentChanged(component["@id"]);
    $canvasService.deleteByIri(component["@id"]);
  }

  function onCopyComponent(component) {
    // Clone in the pipeline.
    const clone = pplModel.cloneComponent($pipeline, component);
    const templateIri = pplModel.component.getTemplateIri(clone);
    const template = $templatesService.getTemplate(templateIri);
    // Add to canvas graph.
    $actions.createComponentCell(template, clone);
  }

  function onEnableDisable(component) {
    const isDisabled = pplModel.component.isDisabled(component);
    pplModel.component.setDisabled(component, !isDisabled);
    $canvasService.updateComponent(jsonld.r.getId(component));
    componentChanged(jsonld.r.getId(component));
  }

  function onDebugToComponent(component) {
    $actions.savePipeline().then(() => {
      $actions.executePipeline({
        "keepDebugData": true,
        "debugTo": pplModel.component.getIri(component)
      }).catch((error) => {
        $statusService.error("Can't start the execution.", error)
      })
    }).catch((error) => {
      $statusService.error("Can't save pipeline.", error)
    })
  }

  function onMappingComponent(component) {
    const iri = pplModel.component.getIri(component);
    const execComponent = execModel.getComponent($execution, iri);
    if (execModel.isMappingEnabled($execution, execComponent)) {
      disableMapping(iri);
    } else {
      enableMapping(iri);
    }
  }

  function disableMapping(iri) {
    const execComponent = execModel.getComponent($execution, iri);
    if (execComponent === undefined) {
      // Component was not executed.
      return;
    }
    execModel.disableMapping($execution, execComponent);
    $canvasService.updateComponent(iri);
    // Propagation - we need to disable all following.
    const conService = pplModel.connection;
    const onConnection = (connection) => {
      if (conService.getSource(connection) === iri) {
        disableMapping(conService.getTarget(connection));
      }
    };
    pplModel.getDataLinks($pipeline).forEach(onConnection);
    pplModel.getRunAfter($pipeline).forEach(onConnection);
  }

  function enableMapping(iri) {
    const execComponent = execModel.getComponent($execution, iri);
    if (execComponent === undefined) {
      // Component was not executed.
      return;
    }
    execModel.enableMapping($execution, execComponent);
    $canvasService.updateComponent(iri);
    // Propagation - we need to enable all out sources.
    const conService = pplModel.connection;
    const onConnection = (connection) => {
      if (conService.getTarget(connection) === iri) {
        enableMapping(conService.getSource(connection));
      }
    };
    pplModel.getDataLinks($pipeline).forEach(onConnection);
    pplModel.getRunAfter($pipeline).forEach(onConnection);
  }

  function onAddComponent(x, y) {
    $dialogsService.selectNewTemplate()
      .then((template) => {
        if (template === undefined) {
          return;
        }
        $actions.createComponent(template.component, x, y);
      });
  }

  function onImportPipeline(x, y) {
    $dialogsService.selectPipeline()
      .then((result) => {
        importPipeline(x, y, result["pipeline"]);
      });
  }

  function importPipeline(x, y, pipelineAsJsonLd) {
    const pipelineToImport = pplModel.createFromJsonLd(pipelineAsJsonLd);
    const {
      "components": components,
      "dataLinks": connections,
      "runAfterLinks": runAfter
    } = pplModel.addPipeline($pipeline, x, y, pipelineToImport);
    loader.loadResources($canvasService, components, connections, runAfter);
  }

  function onConnectionToBlank(source, sourcePort, x, y) {
    const templateIri = pplModel.component.getTemplateIri(source);
    $dialogsService.selectNewTemplate(templateIri, sourcePort)
      .then((template) => {
        if (template === undefined) {
          return;
        }
        $actions.createComponentAfter(
          source, sourcePort,
          template.component, x, y,
          template.portBinding);
      });
  }

  function onCreateTemplate(cell, component) {
    const template = $actions.getComponentTemplate(component);
    const configuration =
      pplModel.getComponentConfiguration($pipeline, component);
    //
    $dialogsService.createTemplate(component, template, configuration)
      .then((data) => {
        // Update template.
        if (data["template"] === undefined) {
          // No template was created.
          return;
        }
        const {
          "template": template,
          "configuration": configuration
        } = data;
        return $templatesService.fetchConfigDesc(template)
          .then((description) => {
            updateComponentTemplate(
              component, template, description, configuration);
          })
      }).catch(() => {}); // To handle dialog close.
  }

  function updateComponentTemplate(
    component, templateIri, templateDescription, templateConfiguration) {
    pplModel.component.setTemplate(component, templateIri);
    $actions.secureConfiguration(component, templateConfiguration);
    const configuration =
      pplModel.getComponentConfiguration($pipeline, component);
    $actions.updateConfigurationToInherit(templateDescription, configuration);
    pplModel.setComponentConfiguration($pipeline, component, configuration);
    // Template may change component color.
    $canvasService.updateComponent(jsonld.r.getId(component));
  }

  function onInsertRunAfter(source, target) {
    const connection = pplModel.createRunAfterLink($pipeline);
    pplModel.connection.setSource(connection, source);
    pplModel.connection.setTarget(connection, target);
    const connectionCell =
      $canvasService.createComponentLinkCell(connection, []);
    $canvasService.registerLinkCell(connection, connectionCell);
    $canvasService.addCells([connectionCell]);
  }

  function onDisableAllLoaders() {
    setDisabledByType(LP.LOADER, true);
    setDisabledByType(LP.EXECUTOR, true);
  }

  function setDisabledByType(type, disabled) {
    pplModel.getComponents($pipeline).forEach((component) => {
      const template = $actions.getComponentTemplate(component);
      const core = $templatesService.getCoreTemplate(template);
      if (core.type.indexOf(type) === -1) {
        return;
      }
      if (pplModel.component.isDisabled(component) === disabled) {
        return;
      }
      pplModel.component.setDisabled(component, disabled);
      $canvasService.updateComponent(jsonld.r.getId(component));
    });
  }

  function onEnableAllLoaders() {
    setDisabledByType(LP.LOADER, false);
    setDisabledByType(LP.EXECUTOR, false);
  }

  function onShowComponentExecutionDetail(component) {
    const iri = pplModel.component.getIri(component);
    const execComponent = execModel.getComponent($execution, iri);
    const execIri = execModel.getIri($execution);
    $dialogsService.debugDetail(component, execComponent, execIri);
  }

  function onShowPortContent(component, port) {
    const iri = pplModel.component.getIri(component);
    const execComponent = execModel.getComponent($execution, iri);
    const dataUnit = execModel.getDataUnit($execution, execComponent, port);
    if (!dataUnit || !dataUnit.debug) {
      $dialogsService.noDebugDataDialog();
      return;
    }
    const execIri = execModel.getIri($execution);
    const executionId = execIri.substring(execIri.lastIndexOf("executions/") + 11);
    const url = "./debug#/" + executionId + "/?path=" +
      encodeURIComponent(dataUnit.debug);
    window.open(url, "_blank");
  }

  return {
    "api": {
      // Canvas events.
      "onPositionChange": onPositionChange,
      "onSourceChange": onSourceChange,
      "onTargetChange": onTargetChange,
      "onConnectionCreate": onConnectionCreate,
      "onDelete": onDelete,
      // Canvas Edit events.
      "onEditComponent": onComponentDetail,
      "onDeleteComponent": onDeleteComponent,
      "onCopyComponent": onCopyComponent,
      "onEnableDisable": onEnableDisable,
      "onDebugToComponent": onDebugToComponent,
      "onMappingComponent": onMappingComponent,
      "onAddComponent": onAddComponent,
      "onImportPipeline": onImportPipeline,
      "onConnectionToBlank": onConnectionToBlank,
      "onCreateTemplate": onCreateTemplate,
      "onInsertRunAfter": onInsertRunAfter,
      // Events from GUI.
      "onDisableAllLoaders": onDisableAllLoaders,
      "onEnableAllLoaders": onEnableAllLoaders,
      "onShowComponentExecutionDetail": onShowComponentExecutionDetail,
      "onShowPortContent": onShowPortContent,
    },
    //
    "setPipeline": (pipeline) => $pipeline = pipeline,
    "setExecution": (execution) => $execution = execution,
    "bind": (canvas, templates, dialogs, status, actions) => {
      $canvasService = canvas;
      $templatesService = templates;
      $dialogsService = dialogs;
      $statusService = status;
      $actions = actions;
    }
  }

});