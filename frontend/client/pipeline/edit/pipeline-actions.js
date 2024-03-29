/**
 * Singleton, contains actions that can be performed on a pipeline.
 */
((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "jquery",
      "@client/app-service/jsonld/jsonld",
      "./model/pipeline-model",
      "./model/execution-model",
      "./canvas/pipeline-loader",
      "@client/app-service/vocabulary",
      "./visual/default-visual",
      "./visual/execution-time-visual",
      "./pipeline-events",
      "../pipeline-api",
      "../../execution/execution-api"
    ], definition);
  }
})((jQuery, jsonld, pplModel, execModel, loader, vocabulary,
    defaultVisual, executionTimeVisual,
    pipelineEvents, pipelinesApi, executionApi) => {

  const LP = vocabulary.LP;

  let activeVisual = defaultVisual;

  const availableVisuals = [defaultVisual, executionTimeVisual];

  let $pipeline;

  let $execution;

  let $executionStatus = {
    "cancelWasRequested" : false
  };

  let $canvasService;

  let $templatesService;

  let $dialogsService;

  let $statusService;

  let $httpService;

  let $locationService;

  let $serviceCallbacks;

  function validateLink(source, target, linkView) {
    // Run after edges can not be connected to any port.
    if (linkView.model.attributes.edgeType === "run_after") {
      return false;
    }
    // Check for type.
    if (source["type"] !== target["type"]) {
      return false;
    }
    // Check for existing connection.
    if (pplModel.doConnectionExists(
      $pipeline,
      source["component"], source["port"],
      target["component"], target["port"])) {
      return false;
    }
    return true;
  }

  // <editor-fold desc="Model information">

  function getInputPortInfo(template, port) {
    if (port.isRuntimeConfiguration) {
      return {
        "type": "input",
        "color": "#FFAAFF",
        "useLabel": template.isDisplayLabels.runtimeConfiguration,
        "binding": port["binding"],
        "content": port["content"],
        "label": port["label"]
      };
    } else if (port.isTaskList) {
      return {
        "type": "input",
        "color": "#CCCCFF",
        "useLabel": template.isDisplayLabels.taskList,
        "binding": port["binding"],
        "content": port["content"],
        "label": port["label"]
      };
    } else {
      return {
        "type": "input",
        "color": "#CCFFCC",
        "useLabel": template.isDisplayLabels.dataInput,
        "binding": port["binding"],
        "content": port["content"],
        "label": port["label"]
      };
    }
  }

  function getOutputPortInfo(template, port) {
    return {
      "binding": port["binding"],
      "content": port["content"],
      "label": port["label"]
    };
  }

  function getConnectionVertices(connection) {
    return pplModel.getVertices($pipeline, connection)
      .filter((resource) => resource !== undefined)
      .map((resource) => ({
        "x": pplModel.connection.getVertexX(resource),
        "y": pplModel.connection.getVertexY(resource),
        "order": pplModel.connection.getVertexOrder(resource)
      })).sort((left, right) => left["order"] - right["order"]);
  }

  // </editor-fold>

  function createComponentCell(template, component) {
    const cell = $canvasService.createComponentCell(template, component);
    $canvasService.registerComponentCell(component, cell);
    $canvasService.addCells([cell]);
  }

  function createComponent(template, x, y) {
    const templateIri = jsonld.r.getId(template);
    return $templatesService.fetchNewConfig(templateIri).then((config) => {
      const component = pplModel.createComponent($pipeline, template, x, y);
      const configuration = jQuery.extend(true, [], config);
      pplModel.setComponentConfiguration($pipeline, component, configuration);
      createComponentCell(template, component);
    });
  }

  function createComponentAfter(
    source, sourcePort, template, x, y, targetPort) {
    const templateIri = jsonld.r.getId(template);
    return $templatesService.fetchNewConfig(templateIri).then((config) => {
      const component = pplModel.createComponent($pipeline, template, x, y);
      const configuration = jQuery.extend(true, [], config);
      pplModel.setComponentConfiguration($pipeline, component, configuration);

      const componentCell =
        $canvasService.createComponentCell(template, component);
      $canvasService.registerComponentCell(component, componentCell);

      const connection = pplModel.createDataLink($pipeline);
      pplModel.connection.setSource(connection, source, sourcePort);
      pplModel.connection.setTarget(connection, component, targetPort);

      const connectionCell =
        $canvasService.createPortLinkCell(connection, []);
      $canvasService.registerLinkCell(connection, connectionCell);

      $canvasService.addCells([componentCell, connectionCell]);
    });
  }

  function getResource(iri) {
    return pplModel.getResource($pipeline, iri);
  }

  function getComponentTemplate(component) {
    const iri = pplModel.component.getTemplateIri(component);
    return $templatesService.getTemplate(iri);
  }

  // Used for older pipelines, where configuration may not be presented.
  function secureConfiguration(component, configuration) {
    let configurationIri = jsonld.r.getIRI(component, LP.HAS_CONFIGURATION);
    if (configurationIri) {
      return;
    }
    //
    pplModel.setComponentConfiguration(
      $pipeline, component, jQuery.extend(true, {}, configuration));
  }

  function updateConfigurationToInherit(description, configuration) {
    const controls = [];
    jsonld.t.iterateResources(description, (resource) => {
      const control = jsonld.r.getIRI(resource, LP.control);
      if (control !== undefined) {
        controls.push(control);
      }
    });
    jsonld.t.iterateResources(configuration, (resource) => {
      for (let key in resource) {
        if (!resource.hasOwnProperty(key)) {
          continue;
        }
        if (controls.indexOf(key) !== -1) {
          jsonld.r.setIRIs(resource, key, LP.INHERIT);
        }
      }
    });
  }

  function canCreateTemplate(component) {
    const template = getComponentTemplate(component);
    return $templatesService.getSupportControl(template);
  }

  function showExecutionMenu() {
    if ($execution) {
      // If we have execution show only if we have debug data.
      return execModel.hasExecutionWorkingData($execution);
    } else {
      // If we have no execution show always.
      return true;
    }
  }

  /**
   * True if mapping for a component can be changed. Ie. if button
   * can be shown to enable/disable the mapping.
   */
  function isMappingAvailable(component) {
    if (!$execution) {
      return false;
    }
    const iri = pplModel.component.getIri(component);
    const execComponent = execModel.getComponent($execution, iri);
    if (execComponent === undefined) {
      return false;
    }
    return execModel.canChangeMapping($execution, execComponent);
  }

  function getTemplate(iri) {
    return $templatesService.getTemplate(iri);
  }

  function getComponentConfiguration(component) {
    return pplModel.getComponentConfiguration($pipeline, component);
  }

  function setComponentConfiguration(component, configuration) {
    pplModel.setComponentConfiguration(
      $pipeline, component, configuration);
  }

  function savePipeline() {
    $canvasService.synchronize();
    const data = pplModel.asJsonLd($pipeline);
    const iri = $pipeline.pipeline.iri;
    return pipelinesApi.savePipeline($httpService, iri, data, true)
      .then(() => {
        $statusService.success("Pipeline saved.");
      });
  }

  function executePipeline(options) {
    if ($execution) {
      options["execution"] = execModel.getIri($execution);
      addMappingToOptions(options);
    }
    const pipelineIri = $pipeline.pipeline.iri;
    return pipelinesApi.executePipeline($httpService, pipelineIri, options)
      .then((response) => {
        if (response.status === 200) {
          $locationService.path("/pipelines/edit/canvas", false)
            .search({
              "pipeline": pipelineIri,
              "execution": response.data.iri
            });
          $serviceCallbacks.onStartExecution(response.data.iri);
          $statusService.success("Execution started.");
        } else {
          $statusService.error("Can't start pipeline.", response);
        }
      });
  }

  function addMappingToOptions(options) {
    const mapped = [];
    const resume = [];

    pplModel.getComponents($pipeline).forEach((component) => {
      const iri = pplModel.component.getIri(component);
      const execComponent = execModel.getComponent($execution, iri);
      if (execComponent === undefined) {
        return;
      }
      if (iri === options["debugTo"]) {
        // Do not map component to debug to.
        return;
      }
      if (execModel.shouldBeMapped($execution, execComponent)) {
        mapped.push({
          "source": iri,
          "target": iri
        })
      }
      if (execModel.shouldBeResumed($execution, execComponent)) {
        resume.push({
          "source": iri,
          "target": iri
        })
      }
    });

    options["mapping"] = mapped;
    options["resume"] = resume;
  }

  function cancelExecution(executionIri) {
    const status = execModel.getExecutionStatus($execution);
    if (status === undefined) {
      // It is queued, so we can just delete it.
      return $httpService.delete(executionIri)
        .then(() => {
          $executionStatus.cancelWasRequested = true;
          return true;
        });
    } else {
      // It is running we need to cancel the execution.
      return executionApi.cancel($httpService, executionIri, "User request")
        .then(() => {
          $executionStatus.cancelWasRequested = true;
          return false;
        });
    }
  }

  const eventsActions = {
    "getTemplate": getTemplate,
    "getComponentConfiguration": getComponentConfiguration,
    "setComponentConfiguration": setComponentConfiguration,
    "createComponentCell": createComponentCell,
    "createComponent": createComponent,
    "createComponentAfter": createComponentAfter,
    "getComponentTemplate": getComponentTemplate,
    "secureConfiguration": secureConfiguration,
    "updateConfigurationToInherit": updateConfigurationToInherit,
    "savePipeline": savePipeline,
    "executePipeline": executePipeline
  };

  return {
    ...pipelineEvents.api,
    //
    // CANVAS API
    //
    "getResource": getResource,
    "getComponentX": pplModel.component.getX,
    "getComponentY": pplModel.component.getY,
    "getComponentTemplateIri": pplModel.component.getTemplateIri,
    "getTemplate": getTemplate,
    "getComponents": pplModel.getComponents,
    "getDataLinks": pplModel.getDataLinks,
    "getRunAfter": pplModel.getRunAfter,
    "getCoreTemplate": (template) => $templatesService.getCoreTemplate(template),
    "getTemplateInputs": (template) => template["inputs"],
    "getTemplateOutputs": (template) => template["outputs"],
    "getInputPortInfo": getInputPortInfo,
    "getOutputPortInfo": getOutputPortInfo,
    "getLinkSourceIri": pplModel.connection.getSource,
    "getLinkSourcePort": pplModel.connection.getSourceBinding,
    "getLinkTargetIri": pplModel.connection.getTarget,
    "getLinkTargetPort": pplModel.connection.getTargetBinding,
    "getConnectionVertices": getConnectionVertices,
    "setConnectionVertices": (connection, vertices) => {
      pplModel.setVertices($pipeline, connection, vertices);
    },
    //
    // CANVAS VISUAL API
    //
    "getComponentLabel":
      (component) => activeVisual.getComponentLabel(component),
    "getComponentDescription":
      (component) => activeVisual.getComponentDescription(component),
    "getComponentFillColor":
      (template, component) => activeVisual.getComponentFillColor(
        template, component),
    "getComponentRectStyle":
      (component) => activeVisual.getComponentRectStyle(component),
    //
    // EDIT API
    //
    "canCreateTemplate": canCreateTemplate,
    "showExecutionMenu": showExecutionMenu,
    "isMappingAvailable": isMappingAvailable,
    //
    // GUI API
    //
    "pipelineFromJsonLd": (data) => {
      $pipeline = pplModel.createFromJsonLd(data);
      for (const visual of availableVisuals) {
        visual.setPipeline($pipeline);
      }
      pipelineEvents.setPipeline($pipeline);
      return $pipeline;
    },
    "executionFromJsonLd": (data, iri) => {
      $execution = execModel.createFromJsonLd(data, iri);
      for (const visual of availableVisuals) {
        visual.setExecution($execution);
      }
      pipelineEvents.setExecution($execution);
      $canvasService.updateAllComponents();
      return $execution;
    },
    "getPipelineLabel": () => {
      return pplModel.getPipelineLabel($pipeline);
    },
    "getPipelineIri": () => {
      return $pipeline.pipeline.iri;
    },
    "getPipelineResource": () => {
      return pplModel.getPipelineResource($pipeline);
    },
    "getExecutionProfile": () => {
      return pplModel.getExecutionProfile($pipeline);
    },
    "isExecutionFinished": () => {
      return execModel.isExecutionFinished($execution);
    },
    "isExecutionCancelable": () => {
      return execModel.isExecutionCancelable($execution) &&
        !$executionStatus.cancelWasRequested;
    },
    "getExecutionStatus": () => {
      return execModel.getExecutionStatus($execution);
    },
    "asJsonLd": () => {
      return pplModel.asJsonLd($pipeline);
    },
    "savePipeline": savePipeline,
    /**
     * Update current visual style.
     */
    "updateVisual": (visualName) => {
      if (visualName === activeVisual.name) {
        return;
      }
      activeVisual = defaultVisual;
      for (const visual of availableVisuals) {
        if (visual.name === visualName) {
          activeVisual = visual;
          break;
        }
      }
      $canvasService.updateAllComponents();
    },
    // Execution API.
    "executePipeline": executePipeline,
    "cancelExecution": cancelExecution,
    //
    "bind": (canvas, templates, dialogs, status, http, location,
             serviceCallbacks) => {
      $canvasService = canvas;
      $templatesService = templates;
      $dialogsService = dialogs;
      $statusService = status;
      $httpService = http;
      $locationService = location;
      $canvasService.setConnectionValidator(validateLink);
      $serviceCallbacks = serviceCallbacks;
      pipelineEvents.bind(canvas, templates, dialogs, status, eventsActions);
      //
      $pipeline = undefined;
      $execution = execModel.emptyModel();
      defaultVisual.setPipeline(undefined);
      defaultVisual.setExecution($execution);
      pipelineEvents.setPipeline(undefined);
      pipelineEvents.setExecution($execution);
    }
  }
});