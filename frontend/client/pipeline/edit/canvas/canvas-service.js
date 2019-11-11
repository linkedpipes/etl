/**
 * TODO Unify links and connections naming.
 */
((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "jointjs",
      "./component",
      "./link",
      "./events"
    ], definition);
  }
})((joint, componentService, linkService, EVENTS) => {

  let $canvas;

  let $serviceData;

  let $userService;

  function registerLinkCell(resource, cell) {
    const id = cell.id;
    $serviceData.iriToId[resource["@id"]] = id;
    $serviceData.idToIri[id] = resource["@id"];
  }

  function registerComponentCell(resource, cell) {
    const id = cell.id;
    $serviceData.idToIri[id] = resource["@id"];
    $serviceData.iriToId[resource["@id"]] = id;
    $serviceData.components.add(resource["@id"]);
  }

  function addCells(cells) {
    $canvas.getGraph().addCells(cells);
  }

  function createComponentCell(template, component) {
    return componentService.create($userService, template, component);
  }

  // Between two ports.
  function createPortLinkCell(connection, vertices) {
    const sourceIri = $userService.getLinkSourceIri(connection);
    const sourcePort = $userService.getLinkSourcePort(connection);
    const targetIri = $userService.getLinkTargetIri(connection);
    const targetPort = $userService.getLinkTargetPort(connection);

    const sourceId = $serviceData.iriToId[sourceIri];
    const targetId = $serviceData.iriToId[targetIri];

    return linkService.createDataLink(
      sourceId, sourcePort, targetId, targetPort, vertices);
  }

  // Between two components.
  function createComponentLinkCell(connection, vertices) {
    const sourceIri = $userService.getLinkSourceIri(connection);
    const targetIri = $userService.getLinkTargetIri(connection);

    const sourceId = $serviceData.iriToId[sourceIri];
    const targetId = $serviceData.iriToId[targetIri];

    return linkService.createRunAfter(sourceId, targetId, vertices);
  }

  function getCellForIri(iri) {
    const id = $serviceData.iriToId[iri];
    if (id === undefined) {
      return;
    }
    return $canvas.getGraph().getCell(id);
  }

  function setEventsDisabled(areDisabled) {
    $serviceData.areEventsDisabled = areDisabled;
  }

  function setInteractive(isInteractive) {
    $canvas.setInteractive(isInteractive);
  }

  function deleteByIri(iri) {
    const cell = getCellForIri(iri);
    if (!cell) {
      return;
    }
    cell.remove();
  }

  function synchronize() {
    // We just need to synchronize vertices.
    $canvas.getGraph().getLinks().forEach((cell) => {
      const connection = getResourceById(cell.id);
      const vertices = cell.attributes.vertices;
      $userService.setConnectionVertices(connection, vertices);
    });
  }

  function bindUserApi(model) {
    $userService = model;
  }

  // <editor-fold desc="Events">

  function onCanvasDelete(cell) {
    if ($serviceData.newConnectionCell === cell) {
      // User draw a connection from port to an empty space.
      $serviceData.newConnectionCell = undefined;
      return;
    }
    const resource = getResourceById(cell.id);
    if (!resource) {
      return;
    }
    // Notify other components, they could listen for delete events
    // as well but this time we provide them with IRI.
    $canvas.getPaper().trigger(EVENTS.delete, resource["@id"]);
    // Remove reference.
    delete $serviceData.iriToId[resource["@id"]];
    delete $serviceData.idToIri[cell.id];
    delete $serviceData.components[resource["@id"]];
    // Notify API.
    $userService.onDelete(resource);
  }

  function onCanvasAdd(cell) {
    if ($serviceData.areEventsDisabled) {
      return;
    }
    if ($serviceData.idToIri[cell.id]) {
      return;
    }
    // A new connection can be added by interactions with ports,
    // but user may quit that operation, so we just store the
    // reference.
    if (cell instanceof joint.dia.Link) {
      $serviceData.newConnectionCell = cell;
    }
  }

  function cellPointerUp(cell) {
    // User may raise the pointer on top of the port, however
    // if there is a newConnectionCell they was dragging a connection.
    if ($serviceData.newConnectionCell) {
      onAddLink($serviceData.newConnectionCell);
    } else if ($serviceData.changeCell) {
      onConnectionChange($serviceData.changeCell);
    }
    // Set all properties related to connections to default.
    $serviceData.newConnectionCell = undefined;
    $serviceData.changeCell = undefined;
    $serviceData.changeCellSource = false;
    $serviceData.changeCellTarget = false;
  }

  function onAddLink(cell) {
    const source = cell.attributes.source;
    const target = cell.attributes.target;
    const type = cell.attributes.type;
    const connection = $userService.onConnectionCreate(
      type,
      getComponentById(source.id), source.port,
      getComponentById(target.id), target.port);
    if (connection === undefined) {
      console.error("Can't create connection model for type:",
        type, source.id, source.port, target.id, target.port);
      return;
    }
    registerLinkCell(connection, cell);
  }

  function onConnectionChange(cell) {
    const connection = getResourceById(cell.id);
    if (connection === undefined) {
      // This can happen when there is created connection to blank.
      return;
    }
    if ($serviceData.changeCellSource) {
      const source = cell.attributes.source;
      $userService.onSourceChange(
        connection,
        getComponentById(source.id),
        source.port);
    }
    if ($serviceData.changeCellTarget) {
      const target = cell.attributes.target;
      $userService.onTargetChange(
        connection,
        getComponentById(target.id),
        target.port);
    }
  }

  function onPositionChange(cell) {
    const resource = getComponentById(cell.id);
    if (resource === undefined) {
      console.warn("Ignored position change for cell:", cell);
      return;
    }
    $userService.onPositionChange(
      resource,
      cell.attributes.position.x,
      cell.attributes.position.y);
  }

  function onSourceChange(cell) {
    $serviceData.changeCell = cell;
    $serviceData.changeCellSource = true;
  }

  function onTargetChange(cell) {
    $serviceData.changeCell = cell;
    $serviceData.changeCellTarget = true;
  }

  function onComponentChanged(iri, component) {
    const cell = getCellForIri(iri);
    if (!component) {
      component = getComponentById(cell.id);
    }
    const templateIri = $userService.getComponentTemplateIri(component);
    const template = $userService.getTemplate(templateIri);
    componentService.update($userService, template, component, cell);
  }

  // </editor-fold>

  // TODO Add cache?
  function getComponentById(id) {
    const resource = getResourceById(id);
    if (resource === undefined) {
      return undefined;
    }
    // Check that the resource is a component.
    if ($serviceData.components.has(resource["@id"])) {
      return resource;
    } else {
      return undefined;
    }
  }

  function getResourceById(id) {
    return $userService.getResource($serviceData.idToIri[id]);
  }

  function updateAllComponents() {
    $serviceData.components.forEach((iri) => {
      updateComponent(iri);
    });
  }

  function updateComponent(iri) {
    const cell = getCellForIri(iri);
    if (!cell) {
      // Component has been removed.
      return;
    }
    const component = getComponentById(cell.id);
    $canvas.getGraph().trigger(EVENTS.changeComponent, iri, component);
  }

  function setConnectionValidator(fnc) {
    const fncWrap = (cellS, magnetS, cellT, magnetT, end, linkView) => {
      // Prevent linking from output ports to input ports
      // within one element.
      if (cellS === cellT) {
        return false;
      }
      // Allow connect of target only to outputs and source
      // only to inputs.
      if (magnetT && magnetT.getAttribute("port-group") === "output") {
        return false;
      }
      if (magnetS && magnetS.getAttribute("port-group") === "input") {
        return false;
      }
      // When reconnecting the port, we may be connected to our output
      // port but other object.
      if (cellS.model.attributes["portsData"] === undefined ||
        cellT.model.attributes["portsData"] === undefined) {
        return false;
      }
      // Source or target are missing for connections, that
      // we added programmatically. We read them from the connection
      // attribute instead.
      let portS;
      if (magnetS) {
        portS = magnetS.getAttribute("port");
      } else {
        portS = linkView.model.attributes.source.port;
      }
      const source = {
        "component": getComponentById(cellS.model.id),
        "port": portS,
        "type": cellS.model.attributes.portsData[portS]["dataType"]
      };
      let portT;
      if (magnetT) {
        portT = magnetT.getAttribute("port");
      } else {
        portT = linkView.model.attributes.target.port;
      }
      // Allow connections only to ports.
      if (portT === undefined) {
        return false;
      }
      const target = {
        "component": getComponentById(cellT.model.id),
        "port": portT,
        "type": cellT.model.attributes.portsData[portT]["dataType"]
      };
      return fnc(source, target, linkView)
    };
    $canvas.setConnectionValidator(fncWrap);
  }

  function factory() {

    $serviceData = {
      "components": new Set(),
      "iriToId": {},
      "idToIri": {},
      "areEventsDisabled": false,
      // A new connection can be created by drag from
      // the port. However we need to report this connection
      // only when it is attached to a target and
      // user is no longer moving with it (ie. cell pointer up
      "newConnectionCell": undefined,
      // User can drag connection across several ports,
      // we need to emit event only when the port is finally attached.
      "changeCell": undefined,
      "changeCellSource": false,
      "changeCellTarget": false
    };

    const serviceApi = {
      /**
       * Return API provided by the user. Data model and receiver of
       * events.
       */
      "getUserApi": () => $userService,
      "bindUserApi": bindUserApi,
      /**
       * Given existing link cell in a graph, register it to the service.
       */
      "registerLinkCell": registerLinkCell,
      /**
       * Given existing component cell in a graph,
       * register it to the service.
       */
      "registerComponentCell": registerComponentCell,
      /**
       * Add a cell to a graph/
       */
      "addCells": addCells,
      /**
       * For given template and component create a cell and return it.
       * The cell needs to be added into graph and service.
       */
      "createComponentCell": createComponentCell,
      /**
       * Create and return cell for dataunit connection.
       * The cell needs to be added into graph and service.
       */
      "createPortLinkCell": createPortLinkCell,
      /**
       * Create and return cell for component connection (run after).
       * The cell needs to be added into graph and service.
       */
      "createComponentLinkCell": createComponentLinkCell,
      /**
       * If true ignore events. Can be used to ignore events about
       * insert when loading.
       */
      "setEventsDisabled": setEventsDisabled,
      /**
       * If set to false render canvas non-interactive.
       */
      "setInteractive": setInteractive,
      /**
       * Remove cell with given IRI. Will cause or remove event
       * to be risen.
       */
      "deleteByIri": deleteByIri,
      /**
       * Synchronize link vertices in pipeline with state on the canvas.
       */
      "synchronize": synchronize,
      /**
       * Invoke update on all components.
       */
      "updateAllComponents": updateAllComponents,
      /**
       * Update a single component.
       */
      "updateComponent": updateComponent,
      // Initialization and settings.
      "bindDirective": (canvas) => bindDirective(service, canvas),
      "setConnectionValidator": setConnectionValidator,
      // Low-level access.
      "getPaper": () => $canvas.getPaper(),
      "getGraph": () => $canvas.getGraph(),
      "setOrigin": (x, y) => $canvas.setOrigin(x, y),
      "getComponentById": getComponentById,
      "getResourceById": getResourceById
    };

    const service = {
      ...serviceApi
    };

    return service;
  }

  function bindDirective(service, canvas) {
    $canvas = canvas;

    canvas.getGraph().on("remove",
      onCanvasDelete.bind(service));
    canvas.getGraph().on("add",
      onCanvasAdd.bind(service));
    canvas.getPaper().on("cell:pointerup",
      cellPointerUp.bind(service));
    canvas.getGraph().on("change:position",
      onPositionChange.bind(service));
    canvas.getGraph().on("change:source",
      onSourceChange.bind(service));
    canvas.getGraph().on("change:target",
      onTargetChange.bind(service));
    canvas.getGraph().on(EVENTS.changeComponent,
      onComponentChanged.bind(service));
  }

  let initialized = false;
  return function (app) {
    if (initialized) {
      return;
    }
    initialized = true;
    app.factory("canvas.service", factory);
  };

});
