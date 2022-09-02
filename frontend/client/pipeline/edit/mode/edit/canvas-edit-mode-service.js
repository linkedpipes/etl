((definition) => {
  if (typeof define === "function" && define.amd) {
    define(["jquery", "jointjs", "../../canvas/events"], definition);
  }
})((jQuery, joint, EVENTS) => {

  let canvas;

  let api;

  const data = {
    "disabled": false,
    "prerequisiteActive": false,
    "pointerDown": false,
    "moveReported": false
  };

  const componentMenu = {
    "visible": false,
    "panning": false,
    "cell": undefined,
    "component": undefined
  };

  const blankMenu = {
    "visible": false,
    "x": undefined,
    "y": undefined
  };

  // Run after edge.
  const prerequisite = {
    "active": false,
    "component": undefined
  };

  // <editor-fold desc="DOM operations">

  function getComponentMenuDom() {
    return jQuery("#componentMenu");
  }

  function getComponentMenuMappingDom() {
    return jQuery("#mapping");
  }

  function getComponentMenuExecuteDom() {
    return jQuery("#execute");
  }

  function getBlankMenuDom() {
    return jQuery("#blankMenu");
  }

  // </editor-fold>

  function onPointerClickCell(cell) {
    if (data.disabled) {
      return;
    }
    const component = canvas.getComponentById(cell.model.id);
    if (component === undefined) {
      return;
    }
    if (prerequisite.active) {
      prerequisite.active = false;
      api.onInsertRunAfter(prerequisite.component, component);
    }
    if (blankMenu.visible) {
      hydeEmptySpaceMenu();
    }
    showComponentMenu(cell, component);
  }

  function showComponentMenu(cell, component) {
    updateMenuComponentPositionAndSize(cell);
    updateOptionalCreateTemplateMenu(component);
    updateOptionalDebugFromMenu(component);
    //
    const menuDom = getComponentMenuDom();
    menuDom.css("display", "inline");
    //
    componentMenu.cell = cell;
    componentMenu.component = component;
    componentMenu.visible = true;
  }

  function updateMenuComponentPositionAndSize(cell) {
    // We can"t use bounding box of the cell as it includes port labels.

    const rectCell = canvas.getGraph().getCell(cell.model.id);
    const paperOffset = canvas.getPaper().viewport.getCTM();

    // We need to update regarding the view port of a paper.
    const boundingBox = rectCell.getBBox();
    boundingBox.x += paperOffset.e;
    boundingBox.y += paperOffset.f;

    const menuDom = getComponentMenuDom();
    menuDom.css("left", boundingBox.x + 20 + "px");
    menuDom.css("top", boundingBox.y + "px");

    updateMenuComponentHeight(boundingBox.height);
  }

  function updateMenuComponentHeight(height) {
    const bottomMenu = jQuery("#bottomBar");
    bottomMenu.css("top", (height - 45) + "px");
  }

  function updateOptionalDebugFromMenu(component) {
    const executeDom = getComponentMenuExecuteDom();
    const mappingDom = getComponentMenuMappingDom();
    if (api.showExecutionMenu()) {
      executeDom.css("display", "inline");
      if (api.isMappingAvailable(component)) {
        mappingDom.css("display", "inline");
      } else {
        mappingDom.css("display", "none");
      }
    } else {
      executeDom.css("display", "none");
      mappingDom.css("display", "none");
    }
  }

  function updateOptionalCreateTemplateMenu(component) {
    const menuDom = getComponentMenuDom();
    if (api.canCreateTemplate(component)) {
      menuDom.find("#createTemplate").css("display", "inline");
    } else {
      menuDom.find("#createTemplate").css("display", "none");
    }
  }

  function onMoveStart() {
    if (data.disabled) {
      return;
    }
    if (componentMenu.visible) {
      hydeComponentMenu();
      componentMenu.panning = true;
    } else {
      componentMenu.panning = false;
    }
    if (blankMenu.visible) {
      hydeEmptySpaceMenu();
    }
  }

  function hydeComponentMenu() {
    getComponentMenuDom().css("display", "none");
    componentMenu.visible = false;
  }


  function hydeEmptySpaceMenu() {
    getBlankMenuDom().css("display", "none");
    blankMenu.visible = false;
  }

  function onMoveEnd() {
    if (data.disabled) {
      return;
    }
    if (componentMenu.panning) {
      showComponentMenu(componentMenu.cell, componentMenu.component);
      componentMenu.panning = false;
    }
  }

  function onEmptyClick(event, x, y) {
    if (data.disabled) {
      return;
    }
    if (prerequisite.active) {
      prerequisite.active = false;
      return;
    }
    if (componentMenu.visible) {
      hydeComponentMenu();
      return;
    }
    if (blankMenu.visible) {
      hydeEmptySpaceMenu();
    } else {
      showEmptySpaceMenu(event, x, y);
    }
  }

  function onPointerDoubleClick(cell) {
    if (data.disabled) {
      return;
    }
    onEditComponent();
  }

  function onComponentUpdate(iri, component) {
    if (!componentMenu.visible ||
      componentMenu.component["@id"] !== iri) {
      return;
    }
    // Update component reference.
    componentMenu.component = component;
    // Refresh component menu.
    showComponentMenu(componentMenu.cell, componentMenu.component);
  }

  function onComponentResize(cell, size) {
    if (cell === undefined || componentMenu.cell === undefined ||
      cell.id !== componentMenu.cell.model.id) {
      return;
    }
    updateMenuComponentHeight(size.height);
  }

  function onCellRemove(cell) {
    if (cell instanceof joint.dia.Link) {
      if (cell.attributes.target.x) {
        // The connection went to a blank area.
        onConnectionToBlank(
          cell, cell.attributes.target.x, cell.attributes.target.y)
      }
      return;
    }
    // Check for component menu.
    if (componentMenu.cell.model === cell) {
      hydeComponentMenu();
    }
  }

  function onConnectionToBlank(link, x, y) {
    const component = canvas.getComponentById(link.attributes.source.id);
    const port = link.attributes.source.port;
    api.onConnectionToBlank(component, port, x, y);
  }

  function showEmptySpaceMenu(event, x, y) {
    const emptySpaceDom = getBlankMenuDom();
    emptySpaceDom.css("left", event.clientX);
    emptySpaceDom.css("top", event.clientY - 65);
    emptySpaceDom.css("display", "inline");
    // Save position of the empty space menu.
    blankMenu.x = x;
    blankMenu.y = y;
    blankMenu.visible = true;
  }

  function onCellPointerDown() {
    data.pointerDown = true;
    data.moveReported = false;
  }

  function onCellPointerMove() {
    if (data.pointerDown && !data.moveReported) {
      data.moveReported = true;
      onMoveStart();
    }
  }

  function onCellPointerUp() {
    if (data.moveReported) {
      onMoveEnd();
    }
    data.pointerDown = false;
    data.moveReported = false;
  }

  // <editor-fold desc="Button actions">

  // TODO Have extra event for edit and for double click.
  function onEditComponent() {
    const component = componentMenu.component;
    const cell = componentMenu.cell;
    api.onEditComponent(cell, component);
  }

  function onDeleteComponent() {
    const component = componentMenu.component;
    api.onDeleteComponent(component);
  }

  function onCopyComponent() {
    const component = componentMenu.component;
    api.onCopyComponent(component);
  }

  function onEnableDisable() {
    const component = componentMenu.component;
    api.onEnableDisable(component);
  }

  function onPrerequisiteComponent() {
    prerequisite.active = true;
    prerequisite.component = componentMenu.component;
  }

  function onDebugToComponent() {
    const component = componentMenu.component;
    api.onDebugToComponent(component);
  }

  function onMappingComponent() {
    const component = componentMenu.component;
    api.onMappingComponent(component);
  }

  function onAddComponent() {
    api.onAddComponent(blankMenu.x, blankMenu.y);
    hydeEmptySpaceMenu();
  }

  function onImportPipeline() {
    api.onImportPipeline(blankMenu.x, blankMenu.y);
    hydeEmptySpaceMenu();
  }

  function onCreateTemplate() {
    api.onCreateTemplate(componentMenu.cell, componentMenu.component);
    hydeComponentMenu();
  }

  // </editor-fold>

  function factory() {
    return {
      "bind": bind,
      "setEnabled": setEnabled,
      "ui": {
        "onEditComponent": onEditComponent,
        "onDeleteComponent": onDeleteComponent,
        "onCopyComponent": onCopyComponent,
        "onEnableDisable": onEnableDisable,
        "onPrerequisiteComponent": onPrerequisiteComponent,
        "onDebugToComponent": onDebugToComponent,
        "onMappingComponent": onMappingComponent,
        "onAddComponent": onAddComponent,
        "onImportPipeline": onImportPipeline,
        "onCreateTemplate": onCreateTemplate
      }
    }
  }

  function setEnabled(enabled) {
    data.disabled = !enabled;
    if (enabled) {
      return;
    }
    hydeComponentMenu();
    hydeEmptySpaceMenu();
    //
    componentMenu.panning = false;
    data.prerequisiteActive = false;
  }

  function bind(_canvas, _api) {
    canvas = _canvas;
    api = _api;

    const graph = canvas.getGraph();
    graph.on(EVENTS.changeComponent, onComponentUpdate);
    graph.on("change:size", onComponentResize);
    graph.on("remove", onCellRemove);

    const paper = canvas.getPaper();
    paper.on("cell:pointerup", onPointerClickCell);
    paper.on(EVENTS.panningStart, onMoveStart);
    paper.on(EVENTS.panningEnd, onMoveEnd);
    paper.on(EVENTS.emptyClick, onEmptyClick);
    paper.on("cell:pointerdblclick", onPointerDoubleClick);

    // We need to track component movement with the menu.
    paper.on("cell:pointerdown", onCellPointerDown);
    paper.on("cell:pointermove", onCellPointerMove);
    paper.on("cell:pointerup", onCellPointerUp);
  }

  let initialized = false;
  return function (app) {
    if (initialized) {
      return;
    }
    initialized = true;
    app.factory("canvas.edit-mode.service", factory);
  };

});
