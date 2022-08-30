/**
 * Provide selection capabilities to the canvas.
 */
((definition) => {
  if (typeof define === "function" && define.amd) {
    define(["./events"], definition);
  }
})((EVENTS) => {

  const service = {
    // Store keys of all selected entries.
    "selection": {},
    // Clear selection when new cell is selected.
    "singleSelectionMode": true
  };

  // We need to pass it to the
  const highlighter = {
    "highlighter": {
      "name": "stroke",
      "options": {
        "padding": 13,
        "rx": 2,
        "ry": 2,
        "attrs": {
          "stroke": "black",
          "stroke-width": 1,
          "stroke-dasharray": "5,5"
        }
      }
    }
  };

  service["addToSelection"] = (view) => {
    const cell = service.graph.getCell(view.model.id);
    if (service.selection[cell.id]) {
      // Already selected.
      return;
    }

    // Check selection mode.
    if (service.singleSelectionMode) {
      service.paper.trigger(EVENTS.selectionClean);
    }

    // Add to selection.
    addCellToSelection(cell, view);
    view.highlight(null, highlighter);
  };

  function addCellToSelection(cell, view) {
    service.selection[cell.id] = view;
    service.paper.trigger(EVENTS.elementSelected, cell);
  }

  service["bind"] = (paper, graph) => {
    service.paper = paper;
    service.graph = graph;

    service.graph.on("remove", onCellRemove);
    service.paper.on(EVENTS.selectionClean, clearAllSelection);
    // The size of highlight element is not changed with the component,
    // so we watch for changes and update manually.
    service.graph.on("change:size", onComponentResize);
  };

  function onCellRemove(cell) {
    const view = service.selection[cell.id];
    if (!view) {
      return;
    }
    delete service.selection[cell.id];
    service.paper.trigger(EVENTS.elementDeselected, cell);
  }

  function clearAllSelection() {
    if (Object.keys(service.selection).length === 0) {
      return;
    }
    for (let key in service.selection) {
      const view = service.selection[key];
      view.unhighlight(null, highlighter);
    }
    service.selection = {};
  }

  function onComponentResize(cellModel, newSize) {
    if (cellModel === undefined) {
      return;
    }
    const cellView = service.selection[cellModel.id];
    if (cellView === undefined) {
      return;
    }
    // Remove and add - I do not know a simpler way how to force
    // size update.
    cellView.unhighlight(null, highlighter);
    cellView.highlight(null, highlighter);
  }

  return service;

});
