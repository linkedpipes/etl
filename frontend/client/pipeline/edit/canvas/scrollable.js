/**
 * Add scroll capability to canvas.
 */
define([
  "jquery",
  "jointjs",
  "backbone",
  "lodash",
  "./events"
], function (jQuery, joint, Backbone, _, EVENTS) {

  const Scrollable = Backbone.View.extend({
    "initialize": initialize,
    "startPanning": startPanning
  });

  function initialize(args) {
    this.options = _.extend({}, _.result(this, "options"), args || {});
    this.positionX = 0;
    this.positionY = 0;
  }

  function startPanning(event) {
    // Check if the event is touch based.
    this.isLowSensitivity = (event.type === "touchstart");

    const normalizedEvent = joint.util.normalizeEvent(event);
    this.clientX = normalizedEvent.clientX;
    this.clientY = normalizedEvent.clientY;

    this.options.status.moved = false;

    // Register events for panning (mouse and touch).
    jQuery(document.body).on({
      "mousemove.panning touchmove.panning": panning.bind(this),
      "mouseup.panning touchend.panning mouseout.panning": stopPanning.bind(this)
    });
  }

  function panning(event) {
    event = joint.util.normalizeEvent(event);
    // Change on X and Y axis.
    const x = event.clientX - this.clientX;
    const y = event.clientY - this.clientY;
    // Check if there was a movement, we need a special attention
    // for touch devices.
    if (shouldIgnoreEvent(x, y, this.isLowSensitivity)) {
      return;
    }
    if (this.isLowSensitivity) {
      if (shouldIgnoreTouchEvent(x, y, this.options.sensitivity)) {
        return;
      } else {
        // User made a move enough to consider it a move.
        // We reset values to mitigate initial "jump".
        this.isLowSensitivity = false;
        this.clientX = event.clientX;
        this.clientY = event.clientY;
        return;
      }
    }
    updatePosition.call(this, event, x, y);

    // Report start of the movement.
    if (!this.options.status.moved) {
      this.options.status.moved = true;
      this.options.paper.trigger(EVENTS.panningStart);
    }

  }

  function shouldIgnoreEvent(x, y,) {
    return x === 0 && y === 0;
  }

  function shouldIgnoreTouchEvent(x, y, sensitivity) {
    return Math.abs(x) + Math.abs(y) < sensitivity;
  }

  function updatePosition(event, x, y) {
    // Update inner position.
    this.positionX += x;
    this.positionY += y;
    // Update position of the paper - move the paper.
    this.options.paper.setOrigin(this.positionX, this.positionY);
    // Store position of this event.
    this.clientX = event.clientX;
    this.clientY = event.clientY;
  }

  function stopPanning() {
    if (this.options.status.moved) {
      this.options.paper.trigger(EVENTS.panningEnd);
    }
    // Remove all panning events.
    jQuery(document.body).off(".panning");
  }

  return {
    "wrap": (paper, status) => new Scrollable({
      "paper": paper,
      "status": status,
      "sensitivity": 16
    })
  }

});
