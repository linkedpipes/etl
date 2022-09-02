/**
 * Contains definition of used events.
 */
((definition) => {
  if (typeof define === "function" && define.amd) {
    define([], definition);
  }
})(() => ({
  // On component selected.
  "elementSelected": "lp:selected"
  // On component deselected.
  , "elementDeselected": "lp:deselected"
  // Request clear selection.
  , "selectionClean": "lp:selection-clean"
  // On panning start.
  , "panningStart": "lp:panningstart"
  // On panning end.
  , "panningEnd": "lp:panningend"
  // Click to en empty space.
  , "emptyClick": "lp:emptyclick"
  // Component definition have changed.
  , "changeComponent": "lp:component:changed"
  // Called when a resource was deleted.
  , "delete": "lp:resource:remove"
}));