//
// Contains definition of a JointJs canvas directive.
//
// Custom events:
//  lp:emptyclick - on click into an empty space
//  lp:panningstart - start of the movement
//  lp:panningend - end of the movement
//  lp:selected - cell selected
//  lp:deselected - cell deselected
//

define([
    'jquery',
    'jointjs',
    'backbone',
    'lodash'
], function (jQuery, joint, Backbone, _) {

    /**
     * @constructor
     */
    var Paper = function (graph, element, interactive, connectionValidator) {
        return new joint.dia.Paper({
            'async': true,
            'el': element,
            'width': '100%',
            'height': '100%',
            'model': graph,
            'gridSize': 20,
            'linkPinning': false,
            'snapLinks': {'radius': 50},
            'defaultLink': new joint.dia.Link({
                'attrs': {'.marker-target': {'d': 'M 10 0 L 0 5 L 10 10 z'}}}),
            'validateConnection': connectionValidator,
            'markAvailable': true,
            'interactive': interactive
        });
    };

    // TODO Unify acess to properties from options
    // in ScrollableView and ComponentSelector

    /**
     * Add scrolling capability to the canvas.
     * @constructor
     */
    var ScrollableView = Backbone.View.extend({
        'options': {'paper': void 0, 'scroll': void 0},
        'initialize': function (args) {
            this.options = _.extend({}, _.result(this, 'options'), args || {});
            this.paper = this.options.paper;
            this.positionX = 0;
            this.positionY = 0;
            // The low sensitivity area should be used on devices
            // with lowered precission (touch). It will ignore small
            // movements.
            this.lowSensitivity = 0;
        },
        'startPanning': function (event) {
            // Check if the event is touch based.
            this.lowSensitivity = (event.type === 'touchstart');
            // Store initial position.
            event = joint.util.normalizeEvent(event);
            this._clientX = event.clientX;
            this._clientY = event.clientY;
            // Register events for panning (mouse and touch).
            jQuery(document.body).on({
                'mousemove.panning touchmove.panning': this.pan.bind(this),
                'mouseup.panning touchend.panning': this.stopPanning.bind(this)
            });
        },
        'pan': function (event) {
            event = joint.util.normalizeEvent(event);
            var x = event.clientX - this._clientX;
            var y = event.clientY - this._clientY;
            // Check if there was a movement, we need a special attention
            // for touch devices.
            if (x === 0 && y === 0) {
                return;
            }
            if (this.lowSensitivity) {
                // In touch mode ignore some minor movement.
                // TODO The values should be part of the personification !
                if (Math.abs(x) < 16 && Math.abs(y) < 16) {
                    // Use is in low sensitivity area from the starting
                    // point.
                    return;
                } else {
                    // Disable for and discard the initial movement, se
                    // there is no jump once user leave the low
                    // sensitivity area.
                    this.lowSensitivity = false;
                    this._clientX = event.clientX;
                    this._clientY = event.clientY;
                    return;
                }
            }
            this.positionX += x;
            this.positionY += y;
            // Update position.
            this.paper.setOrigin(this.positionX, this.positionY);
            this._clientX = event.clientX;
            this._clientY = event.clientY;
            // Report start of the movement.
            if (!this.options.status.moved) {
                this.paper.trigger('lp:panningstart');
            }
            this.options.status.moved = true;
        },
        'stopPanning': function (event) {
            if (this.options.status.moved) {
                this.paper.trigger('lp:panningend');
            }
            jQuery(document.body).off('.panning');
        }
    });

    // TODO Update to enable multiple selection.
    var ComponentSelector = Backbone.View.extend({
        'className': 'component-selection',
        'events': {'mousedown .action': 'onAction'},
        'initialize': function (args) {
            this.options = _.extend({}, _.result(this, 'options'), args || {});
            // Delete other instances of selectors.
            this.options.paper.trigger('component-select:clean');
            this.options.paper.$el.append(this.el);
            // Listener for events related to resource change.
            this.listenTo(this.options.graph, 'change:position', this.update);
            this.listenTo(this.options.graph, 'change:size', this.update);
            // Listeners for removing the item
            this.listenTo(this.options.cell, 'remove', this.remove);
            this.listenTo(this.options.paper, 'component-select:clean',
                    this.remove);
            // React to zoom or movement.
            this.listenTo(this.options.paper, 'scale translate', this.update);

            // Add current model (component) to selection.
            this.options.selection[this.options.cell.id] = true;
            this.options.paper.trigger('lp:selected', this.cell);
            //
            this.update();
        },
        'update': function () {
            // Computes position of rectangle on the screen.
            var canvasPosition = this.options.paper.viewport.getCTM();
            var boundingBox = this.options.cell.getBBox();
            boundingBox.x += canvasPosition.e;
            boundingBox.y += canvasPosition.f;
            //
            boundingBox.x += -8;
            boundingBox.y += -8;
            boundingBox.width += 26;
            boundingBox.height += 18;
            this.$el.css({
                left: boundingBox.x,
                top: boundingBox.y,
                width: boundingBox.width,
                height: boundingBox.height
            });
        },
        'remove': function (event) {
            delete this.options.selection[this.options.cell.id];
            this.options.paper.trigger('lp:deselected', this.cell);
            // Delete this selection bounding box.
            Backbone.View.prototype.remove.apply(this, arguments);
        }
    });

    function directiveFactory() {
        return {
            'restrict': 'E',
            'scope': {'api': '='},
            'template': '',
            'link': function ($scope, element, attrs) {

                // Represent an internal component status.
                var status = {
                    /**
                     * In case of button down on empty space we can
                     * initialize scroll, or we can fire onEmptySpaceClick.
                     * This structure is used to store infomarion about
                     * empty space click and detect which possiblity to
                     * choose.
                     */
                    'moved': false,
                    /**
                     * True if pointer went down on the blank space.
                     */
                    'downblank': false,
                    /**
                     * Interactive mode.
                     */
                    'interactive': true
                };

                var graph = new joint.dia.Graph;

                // For internal use, store IDs of selected cells.
                var selection = {};

                function isInteractive() {
                    return status.interactive;
                }

                function validateConnection(
                        cellS, magnetS, cellT, magnetT, end, linkView) {
                    return status.connectionValidator(cellS, magnetS, cellT,
                            magnetT, end, linkView);
                }

                var paper = new Paper(graph, element, isInteractive,
                        validateConnection);

                // Add scroll capability.
                var scrollableView = new ScrollableView({
                    'paper': paper,
                    'status': status
                });

                scrollableView.$el.css({
                    'width': '100%',
                    'height': '100%'
                }).appendTo(element);

                paper.on('blank:pointerdown', function (event) {
                    status.moved = false;
                    status.downblank = true;
                    scrollableView.startPanning(event);
                });

                paper.on('blank:pointerup', function (event, x, y) {
                    if (!status.moved && status.downblank) {
                        paper.trigger('component-select:clean');
                        paper.trigger('lp:emptyclick', event, x, y);
                        status.downblank = false;
                    }
                });

                paper.on('cell:pointerdown', function (view) {
                    // TODO Enable more granularity not only interactive
                    if (!status.interactive) {
                        return;
                    }
                    var notSelected = selection[view.model.id] === undefined;
                    if (view.model.getBBox && notSelected) {
                        new ComponentSelector({
                            'cell': graph.getCell(view.model.id),
                            'paper': paper,
                            'graph': graph,
                            'selection': selection
                        }).render();
                    }
                });

                // Disable right click.
                paper.el.oncontextmenu = function (event) {
                    event.preventDefault();
                };

                // TODO Move API export controler function.

                $scope.api.getGraph = function () {
                    return graph;
                };

                $scope.api.getPaper = function () {
                    return paper;
                };

                $scope.api.setOrigin = function (x, y) {
                    paper.setOrigin(x, y);
                    scrollableView.positionX = x;
                    scrollableView.positionY = y;
                };

                $scope.api.setInteractive = function (interactive) {
                    status.interactive = interactive;
                    // TODO Made this optional as selection can be
                    // used by different modes.
                    if (!status.interactive) {
                        paper.trigger('component-select:clean');
                    }
                };

                $scope.api.setConnectionValidator = function (fnc) {
                    status.connectionValidator = fnc;
                };

            }
        };
    }

    return function (app) {
        app.directive('lpCanvas', [directiveFactory]);
    };

});