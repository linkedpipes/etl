define([
    'jquery',
    'jointjs',
    'backbone'
], function ($, joint, Backbone) {

    /**
     * Create and return port specification. The information is cached in the
     * given template object.
     */
    var createPorts = function (component, template) {
        var inPorts = [];
        var outPorts = [];
        var portsData = {};

        if (template['_ports']) {
            inPorts = template['_ports']['inPorts'];
            outPorts = template['_ports']['outPorts'];
            portsData = template['_ports']['portsData'];
        } else {
            // Inlcude labels only if there is more than one data unit.
            if (template['inputs']) {
                template['inputs'].forEach(function (port) {
                    portsData[port['binding']] = {
                        'id': port['binding'],
                        'label': port['label'],
                        'dataType': port['type'],
                        'useLabel': template['inputs'].length > 1
                    };
                    inPorts.push(port['binding']);
                });
            }
            if (template['outputs']) {
                template['outputs'].forEach(function (port) {
                    portsData[port['binding']] = {
                        'id': port['binding'],
                        'label': port['label'],
                        'dataType': port['type'],
                        'useLabel': template['outputs'].length > 1
                    };
                    outPorts.push(port['binding']);
                });
            }
            // Store template.
            template['_ports'] = {
                'inPorts': inPorts,
                'outPorts': outPorts,
                'portsData': portsData
            };
        }

        return {
            'inPorts': inPorts,
            'outPorts': outPorts,
            'portsData': portsData
        };
    };

    /**
     * Create component view model instance.
     */
    var createComponentCell = function (component, template, componentService) {
        // Create ports in not presented in cache.
        var ports = createPorts(component, template);
        // Create view.
        var cell = new ComponenModel({
            position: {
                'x': componentService.getX(component),
                'y': componentService.getY(component)
            },
            inPorts: ports['inPorts'],
            outPorts: ports['outPorts'],
            portsData: ports['portsData'],
            attrs: {
                '.inPorts circle': {
                    'fill': '#CCFFCC',
                    'magnet': 'passive',
                    'type': 'input'},
                '.outPorts circle': {
                    'fill': '#FFFFCC',
                    'type': 'output'}
            }
        });
        //
        updateComponentCell(cell, component, template, componentService);
        return cell;
    };

    /**
     * Update component view model (cell).
     */
    var updateComponentCell = function (cell, component, template,
            componentService) {
        // Construct label.
        var label = componentService.getLabel(component);
        var description = componentService.getDescription(component);
        if (description) {
            label += '\n';
            label += description;
        }
        // Calculate size.
        var portCount = Math.max(cell.attributes.inPorts.length,
                cell.attributes.outPorts.length, 1);
        var labelSplit = label.split('\n');
        var height = Math.max(portCount * 25 + 10, labelSplit.length * 23);
        var width = 0;
        labelSplit.forEach(function (line) {
            width = Math.max(width, line.length * 10);
        });
        // Some adjustment of size.
        if (width <= 150) {
            width += 20;
        } else if (width >= 200) {
            width -= 20;
        }
        // Set properties.
        if (description) {
            cell.attr('.label', {
                'text': label,
                'ref-y': '20%',
                'y-alignment': 'bottom',
                'x-alignment': 'left'
            });
        } else {
            // If there is no description then center the label to the middle.
            cell.attr('.label', {
                'text': label,
                'ref-y': '50%',
                'y-alignment': 'middle',
                'x-alignment': 'left'
            });
        }
        cell.resize(width, height);
        // Color.
        // Read color.
        var color = componentService.getColor(component);
        if (!color) {
            color = template.color;
        }
        cell.attr('rect', {fill: color});
    };

    /**
     * Custom component model to allow better customizaiton of component look.
     *
     * Port can have useLabel property that determines if the label
     * is visible or not.
     */
    var ComponenModel = joint.shapes.devs.Model.extend({
        getPortAttrs: function (portName, index, total, selector, type) {

            var port = this.attributes.portsData[portName];

            var portClass = 'port' + index;
            var portSelector = selector + '>.' + portClass;
            var portLabelSelector = portSelector + '>.port-label';
            var portBodySelector = portSelector + '>.port-body';

            var attrs = {};
            attrs[portLabelSelector] = {
                'text': port['useLabel'] ? port['label'] : ''
            };
            attrs[portBodySelector] = {
                'port': {
                    'id': portName || _.uniqueId(type),
                    'type': type
                }
            };
            attrs[portSelector] = {
                'ref': '.body', 'ref-y': (index + 0.5) * (1 / total)
            };

            if (selector === '.outPorts') {
                attrs[portSelector]['ref-dx'] = 0;
            }

            return attrs;
        }
    });

    /**
     * Add scrolling capability to the canvas.
     */
    var ScrollableView = Backbone.View.extend({
        options: {paper: void 0, scroll: void 0},
        initialize: function (args) {
            this.options = _.extend({}, _.result(this, 'options'), args || {});
            this.paper = this.options.paper;
            this.positionY = 0;
            this.positionX = 0;
        },
        startPanning: function (event) {
            // Store initial position.
            event = joint.util.normalizeEvent(event);
            this._clientX = event.clientX;
            this._clientY = event.clientY;
            // Register events for panning (mouse and touch).
            $(document.body).on({
                'mousemove.panning touchmove.panning': this.pan.bind(this),
                'mouseup.panning touchend.panning': this.stopPanning
            });
        },
        pan: function (event) {
            event = joint.util.normalizeEvent(event);
            var x = event.clientX - this._clientX;
            var y = event.clientY - this._clientY;
            this.positionY += y;
            this.positionX += x;
            // Update position.
            this.paper.setOrigin(this.positionX, this.positionY);
            this._clientX = event.clientX;
            this._clientY = event.clientY;
            // Sometimes this event is called even if there is no change
            // in position.
            if (x !== 0 || y !== 0) {
                if (!this.options.scroll.moved) {
                    // Report start of the movement to the owner.
                    this.options.api.onMoveStart();
                }
                this.options.scroll.moved = true;
            }
        },
        stopPanning: function (event) {
            $(document.body).off('.panning');
        }
    });

    /**
     * Represent selection of a component as a bounding box.
     */
    var ComponentSelector = Backbone.View.extend({
        className: 'component-selection',
        events: {'mousedown .action': 'onAction'},
        options: {
            cellView: void 0, // View of the selected item.
            status: void 0, // Scope used to store infomation about selection.
            api: void 0
        },
        initialize: function (args) {
            this.options = _.extend({}, _.result(this, 'options'), args || {});
            _.defaults(this.options, {
                cell: this.options.cellView.model,
                paper: this.options.cellView.paper,
                graph: this.options.cellView.paper.model
            });
            this.status = this.options.status;
            // Delete other instances of selectors.
            this.options.paper.trigger('component-select:clean');
            this.options.paper.$el.append(this.el);
            // Listener for event - we need this to update out position if the
            this.listenTo(this.options.graph, 'all', this.update);
            // Listeners for removing the item
            this.listenTo(this.options.cell, 'remove', this.remove);
            this.listenTo(this.options.paper, 'component-select:clean',
                    this.remove);
            // React to zoom or movement.
            this.listenTo(this.options.paper, 'scale translate', this.update);
            // Add current model (component) to selection.
            this.status.selection.push(this.options.cellView.model);
        }, update: function () {
            // Computes position of rectangle on the screen.
            var canvasPosition = this.options.paper.viewport.getCTM();
            var boundingBox = this.options.cell.getBBox();
            boundingBox.x += canvasPosition.e;
            boundingBox.y += canvasPosition.f;
            //
            this.options.api.onMoveSelected(this.options.cell.id,
                    boundingBox.x, boundingBox.y);
            //
            boundingBox.x += -6;
            boundingBox.y += -10;
            boundingBox.width += 22;
            boundingBox.height += 20;
            this.$el.css({
                left: boundingBox.x,
                top: boundingBox.y,
                width: boundingBox.width,
                height: boundingBox.height
            });
        }, remove: function (event) {
            // Remove item from the selection list.
            this.status.selection.splice(this.status.selection.indexOf(
                    this.options.cellView.model), 1);
            // Delete this selection bounding box.
            Backbone.View.prototype.remove.apply(this, arguments);
            this.options.api.onUpdateSelection();
        }}
    );

    /**
     * Function used to validate connections upon creation.
     *
     * @param cellViewS Source view.
     * @param magnetS Source magnet.
     * @param cellViewT Target view.
     * @param magnetT Target magnet.
     * @param end
     * @param linkView Link view.
     * @returns True if connection can be created.
     */
    var validateConnection = function (cellViewS, magnetS, cellViewT, magnetT,
            end, linkView) {
        // Return false for run after edges.
        if (linkView.model.attributes.edgeType === 'run_after') {
            return false;
        }
        // Prevent linking from input ports.
        if (!magnetS || magnetS.getAttribute('type') === 'input') {
            return false;
        }
        // Prevent linking from output ports to input ports within one element
        // and loops.
        if (cellViewS === cellViewT) {
            return false;
        }
        // Only link to target elements.
        if (!magnetT || magnetT.getAttribute('type') !== 'input') {
            return false;
        }
        // Check for type - this can be slow, we may wan't to use hashing,
        // or some other method?
        var typesS = cellViewS.model.attributes.portsData[
                magnetS.getAttribute('port')]['dataType'];
        var typesT = cellViewT.model.attributes.portsData[
                magnetT.getAttribute('port')]['dataType'];
        for (var i = 0; i < typesS.length; ++i) {
            for (var j = 0; j < typesT.length; ++j) {
                if (typesS[i] === typesT[j]) {
                    return true;
                }
            }
        }
        return false;
    };

    /**
     * Definition of a custom canvas (Paper).
     */
    var Paper = function (graph, element) {
        return new joint.dia.Paper({
            'async': true,
            'el': element,
            'width': '100%',
            'height': '100%',
            'model': graph,
            /**
             * Density of grid we cam move around.
             */
            'gridSize': 20,
            /**
             * Do not let user drop link on blank paper.
             */
            'linkPinning': false,
            /**
             * Snap link to closes target, it's more user friendly.
             */
            'snapLinks': {'radius': 50},
            // Use to connect ports.
            'defaultLink': new joint.dia.Link({
                'attrs': {'.marker-target': {d: 'M 10 0 L 0 5 L 10 10 z'}}}),
            'validateConnection': validateConnection,
            /**
             * Enable target/port highlights, utilize validateConnection
             * funciton.
             */
            'markAvailable': true
        });
    };

    /**
     * Empty defintion of exposed API.
     */
    var API = {
        /**
         * Report click on component.
         */
        'onClick': function (id) {},
        /**
         * Report click on empty space.
         */
        'onEmptyClick': function (x, y, event) {},
        /**
         * Can be used only on selected objects.
         */
        'onDoubleClick': function (id) {},
        /**
         * Called on selection change.
         */
        'onUpdateSelection': function (id) {},
        /**
         * Called on creation of new connection.
         */
        'onNewConnection': function (type, id, source, sourcePort, target, targetPort) {},
        /**
         * Called on position change.
         */
        'onPositionChange': function (id, x, y) {},
        /**
         * Called on change in connection endpoints.
         */
        'onConnectionChange': function (id, source, sourcePort, target, targetPort) {},
        /**
         * Called on delete, report removed component and connections!
         */
        'onDelete': function (id) {},
        /**
         * Called when the target of a connection end is dropped on a blank paper.
         *
         * The first parametr is the ID of a connection object, same as in onDelete. The second and third
         * parameters are the position of unattached pipeline end.
         */
        'onConnectionToEmpty': function (id, x, y) {},
        /**
         * Called when use starts moving canvas.
         */
        'onMoveStart': function () {},
        /**
         * Called when position of selected component changed, is called as the position is changing.
         */
        'onMoveSelected': function (id, x, y) {},
        /**
         * Create a new component view for given component and view. ID is optional.
         */
        'addComponent': function (component, template, componentService) {},
        /**
         * Update component view.
         */
        'updateComponent': function (id, component, template, componentService) {},
        /**
         * Used to update visuals on the component that are not set in the pipeline.
         */
        'updateComponentVisual': function (id, parameters) {},
        /**
         * Remove component of given id. onDelete is called on the component and all attached connections.
         */
        'deleteComponent': function (id) {},
        /**
         * Add connection of given type.
         *
         * Supported types:
         *  link
         *  control
         */
        'addConnection': function (source, sourcePort, target, targetPort, vertices, type) {},
        /**
         * Vertices for connection with given id.
         */
        'getVertices': function (id) {},
        /**
         * Delete content of the canvas.
         */
        'clear': function () {},
        /**
         *
         */
        'loadStart': function () {},
        /**
         *
         */
        'loadEnd': function () {},
        /**
         * Bounding box of object on the screen, with respect to the pipeline left top corner.
         */
        'getScreenBoundingBox': function (id) {},
        /**
         * Return list of ID of all connections.
         */
        'getConnections': function () {},
        /**
         * Set position of the view left top corner.
         */
        'setScreen': function (x, y) {}
    };

    /**
     * Directive that can be used to create pipeline canvas.
     */
    function directiveCanvas() {
        return {
            restrict: 'E',
            scope: {'api': '='},
            template: '',
            link: function ($scope, element, attrs) {

                $scope.api = $.extend({}, API, $scope.api);

                $scope.graph = new joint.dia.Graph;

                $scope.status = {
                    /**
                     * If true we are loading a pipeline.
                     * Reaction on events is thus disabled.
                     */
                    'loading': false,
                    /**
                     * Selected item.
                     */
                    'selection': [],
                    /**
                     * Used for runAfter (prerequisity) mode.
                     */
                    'runAfter': {
                        'active': false,
                        'targetModel': null
                    },
                    /**
                     * In case of button down on empty space we can
                     * initialize scroll, or we can fire onEmptySpaceClick.
                     * This structure is used to store infomarion about
                     * empty space click and detect which possiblity to choose.
                     */
                    'scroll': {
                        'moved': false
                    }
                };

                $scope.paper = new Paper($scope.graph, element);

                // Add scrollable view to canvas.
                $scope.scrollableView = new ScrollableView({
                    'paper': $scope.paper,
                    'scroll': $scope.status.scroll,
                    'api': $scope.api
                });
                $scope.scrollableView.$el.css({
                    'width': '100%', 'height': '100%'}).appendTo(element);

                // Disable right click context menu.
                $scope.paper.el.oncontextmenu = function (event) {
                    event.preventDefault();
                };

                //
                // Register events.
                //

                $scope.graph.on('change:position', function (model) {
                    $scope.api.onPositionChange(model.id,
                            model.attributes.position.x,
                            model.attributes.position.y);
                });

                $scope.graph.on('change:source', function (model) {
                    var source = model.attributes.source;
                    onConnectionChange(model.id, source.id, source.port,
                            null, null);
                });

                $scope.graph.on('change:target', function (model) {
                    var target = model.attributes.target;
                    $scope.api.onConnectionChange(model.id, null, null,
                            target.id, target.port);
                });

                $scope.graph.on('add', function (model) {
                    if ($scope.status.loading) {
                        return;
                    }
                    // Components are added only via our interface (API),
                    // however connections can be added by user, so we
                    // need to check and notify here.
                    if (model instanceof joint.dia.Link) {
                        var source = model.attributes.source;
                        var target = model.attributes.target;
                        $scope.api.onNewConnection(model.attributes.type,
                                model.id, source.id, source.port,
                                target.id, target.port);
                    }
                });

                $scope.graph.on('remove', function (model) {
                    if ($scope.status.loading) {
                        return;
                    }
                    //
                    if (model instanceof joint.dia.Link) {
                        if (model.attributes.target.x) {
                            $scope.api.onConnectionToEmpty(model.id,
                                    model.attributes.target.x,
                                    model.attributes.target.y);
                        } else {
                            $scope.api.onDelete(model.id, model);
                        }
                    } else {
                        $scope.api.onDelete(model.id, model);
                    }
                });

                $scope.paper.on('cell:pointerdown', function (view) {
                    $scope.api.onClick(view.model.id);
                });

                // pointerclick - does not highlight after component movement.
                $scope.paper.on('cell:pointerup', function (view) {
                    if (view.model.getBBox) {
                        new ComponentSelector({
                            cellView: view,
                            status: $scope.status,
                            api: $scope.api
                        }).render();
                        //
                        $scope.api.onUpdateSelection(view.model.id);
                    }
                });

                // pointerclick - cause opening after selection.
                $scope.paper.on('cell:pointerdblclick ', function (view) {
                    // Can be used only on selected object.
                    if ($.inArray(view.model, $scope.status.selection) !== -1) {
                        $scope.api.onDoubleClick(view.model.id);
                    }
                });

                // The blank:pointerup is fired if clicked anywhere on the page.
                // But we want to react only if fired on the canvas.
                // So in order to make it works propertly, we need to check
                // that bwfore blank:pointerup we got blank:pointerdown.
                var blank_pointerdown = false;

                // pointerdown - scroll.
                $scope.paper.on('blank:pointerdown', function (event) {
                    blank_pointerdown = true;
                    // Cancel selection.
                    if ($scope.status.runAfter.active) {
                        $scope.status.runAfter.active = false;
                    }
                    // Start panning.
                    $scope.status.scroll.moved = false;
                    $scope.scrollableView.startPanning(event);
                });

                // We need to use pointerup here not pointerclick as
                // pointerclick is not fired on mobil devices.
                $scope.paper.on('blank:pointerup', function (event, x, y) {
                    if (!blank_pointerdown) {
                        return;
                    }
                    blank_pointerdown = false;
                    if ($scope.status.scroll.moved) {
                        return;
                    }
                    // Do some action.
                    if ($scope.status.selection.length > 0) {
                        // Delesect all.
                        $scope.paper.trigger('component-select:clean');
                        return;
                    } else {
                        $scope.api.onEmptyClick(x, y, event);
                    }
                });
            },
            controller: ['$scope', function ($scope) {

                    $scope.api.addComponent = function (component, template,
                            componentService) {
                        var cell = createComponentCell(component, template,
                                componentService);
                        $scope.graph.addCell(cell);
                        return cell.id;
                    };

                    $scope.api.updateComponent = function (id, component,
                            template, componentService) {
                        var cell = $scope.graph.getCell(id);
                        if (!cell) {
                            console.log('Missing cell for id:', id);
                            return;
                        }
                        //
                        updateComponentCell(cell, component, template,
                                componentService);
                    };

                    $scope.api.updateComponentVisual = function (id,
                            parameters) {
                        var cell = $scope.graph.getCell(id);
                        if (!cell) {
                            console.log('Missing cell for id:', id);
                            return;
                        }
                        //
                        if (parameters.stroke) {
                            cell.attr('rect', {
                                'stroke': parameters.stroke.color,
                                'stroke-width': parameters.stroke.width
                            });
                        }
                    };

                    $scope.api.deleteComponent = function (id) {
                        var cell = $scope.graph.getCell(id);
                        if (!cell) {
                            console.log('Missing cell for id:', id);
                            return;
                        }
                        //
                        cell.remove();
                    };

                    $scope.api.addConnection = function (source, sourcePort,
                            target, targetPort, vertices, type) {
                        var cell;
                        if (type === 'link') {
                            cell = new joint.dia.Link({
                                source: {'id': source, 'port': sourcePort},
                                target: {'id': target, 'port': targetPort},
                                type: type,
                                attrs: {
                                    '.marker-target': {
                                        d: 'M 10 0 L 0 5 L 10 10 z'
                                    }
                                },
                                vertices: vertices
                            });
                        } else if (type === 'control') {
                            cell = new joint.dia.Link({
                                source: {'id': source},
                                target: {'id': target},
                                type: type,
                                attrs: {
                                    '.connection': {
                                        stroke: 'blue'
                                    },
                                    '.marker-target': {
                                        fill: 'yellow',
                                        d: 'M 10 0 L 0 5 L 10 10 z'
                                    }
                                },
                                vertices: vertices
                            });
                        } else {
                            console.log('Unknown connection type:', type);
                        }
                        $scope.graph.addCell(cell);
                        return cell.id;
                    };

                    $scope.api.getVertices = function (id) {
                        var cell = $scope.graph.getCell(id);
                        if (!cell) {
                            console.log('Missing cell for id:', id);
                            return;
                        }
                        //
                        return cell.attributes.vertices;
                    };

                    $scope.api.clear = function () {
                        $scope.graph.clear();
                    };

                    $scope.api.loadStart = function () {
                        $scope.status.loading = true;
                    };

                    $scope.api.loadEnd = function () {
                        $scope.status.loading = false;
                    };

                    $scope.api.getScreenBoundingBox = function (id) {
                        var cell = $scope.graph.getCell(id);
                        if (!cell) {
                            console.log('Missing cell for id:', id);
                            return;
                        }
                        //
                        var paperOffset = $scope.paper.viewport.getCTM();
                        var boundingBox = cell.getBBox();
                        boundingBox.x += paperOffset.e;
                        boundingBox.y += paperOffset.f;
                        return boundingBox;
                    };

                    $scope.api.getConnections = function () {
                        var result = [];
                        $scope.graph.getLinks().forEach(function (model) {
                            result.push(model.id);
                        });
                        return result;
                    };

                    $scope.api.setScreen = function (x, y) {
                        $scope.paper.setOrigin(x, y);
                        //
                        $scope.scrollableView.positionX = x;
                        $scope.scrollableView.positionY = y;
                    };

                }]
        };
    }

    return function register(app) {
        app.directive('pipelineCanvas', [directiveCanvas]);
    };

});