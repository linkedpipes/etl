//
// Provides functionality that enable usage of pipelines on the canvas.
// Provide support for add/delete/clone/etc... operation on the pipeline.
// Custom events:
//  lp:resource:remove - Called whenever a component is removed, parameter
//      is component IRI. Is fired before the component is removed
//      from the pipeline.
//  lp:component:update - Used to notify all about change in the component,
//      parameter is the component IRI, the second parameter is the component
//      and it's optional.
//

define([
    'jquery',
    'jointjs',
    'lodash',
    'lp'
], function (jQuery, joint, _, lp) {

    /**
     * Definition of a component JointJs shape.
     */
    var ComponenModel = joint.shapes.basic.Generic.extend(_.extend({},
            joint.shapes.basic.PortsModelInterface, {
                'markup': '<g class="rotatable"><g class="scalable">\
                           <rect class="body"/></g>\
                           <text class="label"/>\
                           <g class="inPorts"/><g class="outPorts"/></g>',
                'portMarkup': '<g class="port port<%= id %>">\
                               <circle class="port-body"/>\
                               <text class="port-label"/></g>',
                'defaults': joint.util.deepSupplement({
                    'type': 'devs.Model',
                    'size': {'width': 1, 'height': 1},
                    'inPorts': [],
                    'outPorts': [],
                    'attrs': {
                        '.': {'magnet': false},
                        '.body': {
                            'width': 150, 'height': 250,
                            'stroke': 'black',
                            'stroke-width': 1
                        },
                        '.port-body': {
                            'r': 10,
                            'magnet': true,
                            'stroke': '#000000'
                        },
                        'text': {
                            'pointer-events': 'none'
                        },
                        '.label': {
                            'text': 'Model',
                            'ref-x': .5,
                            'ref-y': 10,
                            'ref': '.body',
                            'text-anchor': 'middle',
                            'fill': '#000000'
                        },
                        '.inPorts .port-label': {
                            'x': -15,
                            'dy': 4,
                            'text-anchor': 'end',
                            'fill': '#000000'
                        },
                        '.outPorts .port-label': {
                            'x': 15,
                            'dy': 4,
                            'fill': '#000000'
                        }
                    }

                },
                        joint.shapes.basic.Generic.prototype.defaults),
                'getPortAttrs': function (portName, index, total, selector, type) {
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
                            'id': portName,
                            'type': type
                        }
                    };
                    attrs[portSelector] = {
                        'ref': '.body',
                        'ref-y': (index + 0.5) * (1 / total)
                    };
                    if (selector === '.outPorts') {
                        attrs[portSelector]['ref-dx'] = 0;
                    }
                    return attrs;
                }
            }));

    function validateConnection(cellS, magnetS, cellT, magnetT, end, linkView) {
        // Return false for run after edges.
        if (linkView.model.attributes.edgeType === 'run_after') {
            return false;
        }
        // Prevent linking from input ports.
        if (!magnetS || magnetS.getAttribute('type') === 'input') {
            return false;
        }
        // Prevent linking from output ports to input ports
        // within one element and loops.
        if (cellS === cellT) {
            return false;
        }
        // Only link to target elements.
        if (!magnetT || magnetT.getAttribute('type') !== 'input') {
            return false;
        }
        // Check for type - this can be slow, we may wan't to
        // use hashing, or some other method?
        var contentS = cellS.model.attributes.portsData[
                magnetS.getAttribute('port')]['dataType'];
        var contentT = cellT.model.attributes.portsData[
                magnetT.getAttribute('port')]['dataType'];
        return lp.dataunit.compatible(contentS, contentT);
    }

    /**
     * Create a port specification for given template. The specification is
     * saved to the template and returned.
     */
    function createPorts(template) {
        if (template['_ports'] !== undefined) {
            return template['_ports'];
        }
        var inPorts = [];
        var outPorts = [];
        var portsData = {};
        // Use labels only if there is more than one data unit.
        if (template['inputs'] !== undefined) {
            template['inputs'].forEach(function (port) {
                portsData[port['binding']] = {
                    'binding': port['binding'],
                    'label': port['label'],
                    'dataType': port['content'],
                    'useLabel': template['inputs'].length > 1
                };
                inPorts.push(port['binding']);
            });
        }
        if (template['outputs'] !== undefined) {
            template['outputs'].forEach(function (port) {
                portsData[port['binding']] = {
                    'binding': port['binding'],
                    'label': port['label'],
                    'dataType': port['content'],
                    'useLabel': template['outputs'].length > 1
                };
                outPorts.push(port['binding']);
            });
        }
        template['_ports'] = {
            'in': inPorts,
            'out': outPorts,
            'data': portsData
        };
        return template['_ports'];
    }

    var service = {
        'canvas': void 0,
        'templateService': void 0,
        'pipeline': void 0,
        /**
         * Service used to manipulate with pipeline.
         */
        'pipelineModel': void 0,
        'data': {
            'idToResource': {},
            'iriToId': {},
            /**
             * Contains IRIs of components.
             */
            'components': {}
        },
        'status': {
            /**
             * If true all events are ignored.
             *
             * TODO Now works only on add event.
             */
            'disableEvents': false
        }
    };

    /**
     * Create and return JointJS element based on given data.
     */
    service.createComponent = function (component, template, componentService) {
        if (template === undefined) {
            console.error('Ignored component without a template: ', template);
            return;
        }
        var ports = createPorts(this.templates.getCoreTemplate(template));
        var cell = new ComponenModel({
            'position': {
                'x': componentService.getX(component),
                'y': componentService.getY(component)
            },
            'inPorts': ports['in'],
            'outPorts': ports['out'],
            'portsData': ports['data'],
            'attrs': {
                '.inPorts circle': {
                    'fill': '#CCFFCC',
                    'magnet': 'passive',
                    'type': 'input'},
                '.outPorts circle': {
                    'fill': '#FFFFCC',
                    'type': 'output'}
            }
        });
        service.updateComponent(cell, component, template, componentService);

        // TODO Document this (ID assignment )
        // and make sure we are using this properly.

        // Set component ID if not set.
        if (component['@id'] === '') {
            this.pipelineModel.component.setIriFromId(
                    this.pipeline, component, cell.id);
        }
        //
        return cell;
    };

    service.createLink = function (source, sourcePort, target, targetPort,
            vertices) {
        return new joint.dia.Link({
            'source': {'id': source, 'port': sourcePort},
            'target': {'id': target, 'port': targetPort},
            'type': 'link',
            'attrs': {
                '.marker-target': {
                    d: 'M 10 0 L 0 5 L 10 10 z'
                }
            },
            'vertices': vertices
        });
    };

    service.createControl = function (source, target, vertices) {
        return new joint.dia.Link({
            'source': {'id': source},
            'target': {'id': target},
            'type': 'control',
            'attrs': {
                '.connection': {
                    stroke: 'blue'
                },
                '.marker-target': {
                    fill: 'yellow',
                    d: 'M 10 0 L 0 5 L 10 10 z'
                }
            },
            'vertices': vertices
        });
    };

    // TODO Remove componentService and made it dependency.

    /**
     * Update JointJS component from given data.
     */
    service.updateComponent = function (cell, component, template,
            componentService) {
        // Construct label.
        var label = componentService.getLabel(component);
        // Use empty string instead of undefined.
        if (label === undefined) {
            label = '';
        }
        var description = componentService.getDescription(component);
        if (description !== undefined) {
            label += '\n';
            label += description;
        }
        // Set properties.
        if (description === undefined) {
            cell.attr('.label', {
                'text': label,
                'ref-y': '50%',
                'y-alignment': 'middle',
                'x-alignment': 'left'
            });
        } else {
            cell.attr('.label', {
                'text': label,
                'ref-y': '20%',
                'y-alignment': 'bottom',
                'x-alignment': 'left'
            });
        }
        // Calculate size.
        var portCount = Math.max(
                cell.attributes.inPorts.length,
                cell.attributes.outPorts.length, 1);
        var labelSplit = label.split('\n');
        var height = Math.max(portCount * 25 + 10, labelSplit.length * 23);
        /**
         * Compute approximation of string width.
         */
        function stringWidth(string) {
            var width = 0;
            for (var index in string) {
                var character = string[index];
                if (character > '0' && character < '9') {
                    width += 8.2;
                    continue;
                }
                switch (character) {
                    case ' ':
                        width += 3;
                        break;
                    case 'i':
                    case 'j':
                    case 'l':
                    case 't':
                        width += 5;
                        break;
                    case 'f':
                    case 'r':
                        width += 5;
                        break;
                    case 's':
                        width += 7;
                        break;
                    case 'w':
                        width += 12;
                        break;
                    case 'm':
                        width += 13;
                        break;
                    default:
                        width += 8.5;
                        break;
                }
                if (character !== ' ' && character === character.toUpperCase()) {
                    width += 4;
                }
            }
            return width;
        }
        var maxLineLen = 0;
        labelSplit.forEach(function (line) {
            var lineLen = stringWidth(line);
            if (lineLen > maxLineLen) {
                maxLineLen = lineLen;
            }
        });
        // Add 30 for ports and as a basic size.
        cell.resize(30 + maxLineLen, height);
        // Color.
        var color = componentService.getColor(component);
        if (color === undefined) {
            color = this.templates.getEffectiveColor(template);
        }
        if (componentService.isDisabled(component)) {
            color = '#f2f2f2';
        }
        cell.attr('rect', {'fill': color});
        cell.trigger('change:size');
    };

    // TODO Resolve conflict between createComponent and insertComponent
    service.insertComponent = function (template, x, y) {
        var component = this.pipelineModel.createComponent(
                this.pipeline, template);
        this.pipelineModel.component.setPosition(component, x, y);
        var cell = this.createComponent(component, template,
                this.pipelineModel.component);
        //
        var id = cell.id;
        this.data.idToResource[id] = component;
        this.data.iriToId[component['@id']] = id;
        this.data.components[component['@id']] = true;
        //
        this.canvas.getGraph().addCell(cell);
        return {
            'component': component,
            'cell': cell
        };
    };

    // TODO Update with createLink
    service.insertConnection = function (source, sourcePort, target, targetPort,
            vertices) {
        var sourceId = this.getCell(source['@id']).id;
        var targetId = this.getCell(target['@id']).id;
        console.log(source, sourceId, target, targetId);
        var cell = this.createLink(
                sourceId, sourcePort,
                targetId, targetPort,
                vertices);
        //
        var connection = this.pipelineModel.connection.createConnection(
                this.pipeline);
        this.pipelineModel.connection.setIriFromId(
                this.pipeline, connection, cell.id);
        this.pipelineModel.connection.setSource(connection,
                source, sourcePort);
        this.pipelineModel.connection.setTarget(connection,
                target, targetPort);
        //
        this.data.idToResource[cell.id] = connection;
        this.data.iriToId[connection['@id']] = cell.id;
        // Add to graph, we need to do this here as
        // so it would not trigger the onAdd - add link functionality.
        // We need to register new link first.
        this.canvas.getGraph().addCell(cell);
    };

    // TODO Unify namening RunAfter, Control
    service.insertRunAfter = function (source, target, vertices) {
        var sourceId = this.getCell(source['@id']).id;
        var targetId = this.getCell(target['@id']).id;
        var cell = this.createControl(
                sourceId,
                targetId,
                vertices);
        //
        var connection = this.pipelineModel.connection.createRunAfter(
                this.pipeline);
        this.pipelineModel.connection.setIriFromId(
                this.pipeline, connection, cell.id);
        this.pipelineModel.connection.setSource(connection, source);
        this.pipelineModel.connection.setTarget(connection, target);
        //
        this.data.idToResource[cell.id] = connection;
        this.data.iriToId[connection['@id']] = cell.id;
        // Add to graph, we need to do this here as
        // so it would not trigger the onAdd - add link functionality.
        // We need to register new link first.
        this.canvas.getGraph().addCell(cell);
    };

    service.loadStart = function () {
        this.status.disableEvents = true;
    };

    service.loadEnd = function () {
        this.status.disableEvents = false;
    };

    /**
     * Load pipeline model into canvas and model. Pipeline must be
     * a full pipeline object in parsed JSON-LD.
     */
    service.loadPipeline = function (pipeline) {

        function min(left, right) {
            if (left === undefined) {
                return right;
            } else if (right === undefined) {
                return left;
            } else if (left < right) {
                return left;
            } else {
                return right;
            }
        }

        console.time('canvasPipeline.load');
        this.loadStart();
        this.pipeline = pipeline;
        var conService = this.pipelineModel.connection;
        var comService = this.pipelineModel.component;
        // Load components.
        var components = this.pipelineModel.getComponents(pipeline);
        if (components.length === 0) {
            // Nothing to load.
            console.timeEnd('canvasPipeline.load');
        }
        var cellsToAdd = [];
        // Used to search for the pipeline left top corner.
        var leftTopX = void 0;
        var leftTopY = void 0;
        var missingTemplates = {};
        components.forEach(function (component) {
            var templateIri = comService.getTemplateIri(component);
            var template = this.templates.getTemplate(templateIri);
            if (template === undefined) {
                // FIXME Missing template !
                console.error('Missing template.', templateIri, component);
                console.timeEnd('canvasPipeline.load');
                if (missingTemplates[templateIri] === undefined) {
                    this.statusService.error({
                        'title': 'Missing template',
                        'message': templateIri
                    });
                    missingTemplates[templateIri] = [];
                }
                missingTemplates[templateIri].push(component);
                return;
            }
            // Create element and store reference.
            var cell = this.createComponent(
                    component, template, comService);
            //
            var id = cell.id;
            this.data.idToResource[id] = component;
            this.data.iriToId[component['@id']] = id;
            this.data.components[component['@id']] = true;
            //
            cellsToAdd.push(cell);
            //
            leftTopX = min(leftTopX, comService.getX(component));
            leftTopY = min(leftTopY, comService.getY(component));
        }.bind(this));
        // Data connections.
        var connections = this.pipelineModel.getConnections(pipeline);
        connections.forEach(function (connection) {
            var source = this.data.iriToId[conService.getSource(connection)];
            var target = this.data.iriToId[conService.getTarget(connection)];
            if (source === undefined || target === undefined) {
                // FIXME Missing template !
                console.log('Invalid connection.', source, target);
                return;
            }
            var vertices = conService.getVerticesView(pipeline, connection);
            var cell = this.createLink(
                    source, conService.getSourceBinding(connection),
                    target, conService.getTargetBinding(connection),
                    vertices);
            cellsToAdd.push(cell);
            //
            var id = cell.id;
            this.data.idToResource[id] = connection;
            this.data.iriToId[connection['@id']] = id;
        }.bind(this));
        // Execution flow connections.
        var runAfter = this.pipelineModel.getRunAfter(pipeline);
        runAfter.forEach(function (connection) {
            var source = this.data.iriToId[conService.getSource(connection)];
            var target = this.data.iriToId[conService.getTarget(connection)];
            if (source === undefined || target === undefined) {
                // FIXME Missing template !
                console.log('Invalid connection.', source, target);
                return;
            }
            var vertices = conService.getVerticesView(pipeline, connection);
            var cell = this.createControl(source, target, vertices);
            cellsToAdd.push(cell);
            //
            var id = cell.id;
            this.data.idToResource[id] = connection;
            this.data.iriToId[connection['@id']] = id;
        }.bind(this));
        //
        console.time('canvasPipeline.load.addCells');
        this.canvas.getGraph().addCells(cellsToAdd);
        console.timeEnd('canvasPipeline.load.addCells');
        this.loadEnd();
        console.timeEnd('canvasPipeline.load');
        // Move to the pipeline left top corner.
        if (leftTopX !== undefined && leftTopY !== undefined) {
            this.canvas.setOrigin(
                    -(leftTopX - 50),
                    -(leftTopY - 50));
        }
    };

    service.insertPipeline = function (pipeline, x, y) {
        console.log('import', x, y);

        // TODO Update and merge with loadPipeline

        var conFacade = this.pipelineModel.connection;
        var comFacade = this.pipelineModel.component;

        // We need to update IRI of imported components.
        var iriToIri = {};
        var model = this.pipelineModel.fromJsonLd(pipeline);

        this.loadStart();

        var cellsToAdd = [];
        var components = this.pipelineModel.getComponents(model);

        // Find left most component.
        var minX = Number.POSITIVE_INFINITY;
        var minY = Number.POSITIVE_INFINITY;
        components.forEach(function (component) {
            minX = Math.min(minX, comFacade.getX(component));
            minY = Math.min(minY, comFacade.getY(component));
        });

        components.forEach(function (component) {
            // Update position.
            comFacade.setPosition(component,
                    comFacade.getX(component) + x - minX,
                    comFacade.getY(component) + y - minY);
            //
            var originalId = component['@id'];
            // Map template.
            var templateIri = this.templates.mapToIri(
                    comFacade.getTemplateIri(component));
            comFacade.setTemplate(component, templateIri);
            if (typeof (templateIri) === 'undefined') {
                // Missing template!
                console.warn('Missing tempalte ignored');
                return;
            }
            var template = this.templates.getTemplate(templateIri);
            // Add a copy of rhe resource to current model.
            this.pipelineModel.addResource(this.pipeline, component);
            // Create canvas representation.
            var cell = this.createComponent(
                    component, template, comFacade);
            //
            var id = cell.id;
            // Update IRI.
            comFacade.setIriFromId(this.pipeline, component, id);
            // Store to mapping.
            this.data.idToResource[id] = component;
            this.data.iriToId[component['@id']] = id;
            this.data.components[component['@id']] = true;
            //
            cellsToAdd.push(cell);
            if (!id) {
                // Skip missing.
                console.warn('Missing coponent', component);
                return;
            }
            // Copy configuration.
            var config = this.pipelineModel.getComponentConfigurationGraph(
                    model, component);
            if (config !== undefined) {
                this.pipelineModel.setComponentConfiguration(this.pipeline,
                        component,
                        comFacade.getIri(component) + '/configuration',
                        config);
            }
            //
            iriToIri[originalId] = component['@id'];
        }.bind(this));

        var connections = this.pipelineModel.getConnections(model);
        connections.forEach(function (connection) {
            var vertices = conFacade.getVerticesView(
                    model, connection);
            // Update verticies positions.
            if (vertices !== undefined) {
                vertices.forEach(function (item) {
                    item.x = item.x + x - minX;
                    item.y = item.y + y - minY;
                });
            }
            //
            var sourceIri = iriToIri[conFacade.getSource(connection)];
            var targetIri = iriToIri[conFacade.getTarget(connection)];
            var source = this.data.iriToId[sourceIri];
            var target = this.data.iriToId[targetIri];
            // Ignore invalid connections.
            if (typeof (source) === 'undefined' ||
                    typeof (target) === 'undefined') {
                console.warn('Ignored invalid connection.', connection);
                return;
            }
            // Add connection to the view.
            var cell = this.createLink(
                    source,
                    conFacade.getSourceBinding(connection),
                    target,
                    conFacade.getTargetBinding(connection),
                    vertices);
            cellsToAdd.push(cell);
            var id = cell.id;
            // Add connection to the model.
            var newConnection = conFacade.createConnection(
                    this.pipeline);
            conFacade.setIriFromId(this.pipeline, newConnection, id);
            conFacade.setSource(newConnection,
                    this.data.idToResource[source],
                    conFacade.getSourceBinding(connection));
            conFacade.setTarget(newConnection,
                    this.data.idToResource[target],
                    conFacade.getTargetBinding(connection));

            this.data.idToResource[id] = newConnection;
            this.data.iriToId[newConnection['@id']] = id;
        }.bind(this));

        var runAfter = this.pipelineModel.getRunAfter(model);
        runAfter.forEach(function (connection) {
            var vertices = conFacade.getVerticesView(
                    model, connection);
            // Update verticies positions.
            if (vertices !== undefined) {
                vertices.forEach(function (item) {
                    item.x = item.x + x - minX;
                    item.y = item.y + y - minY;
                });
            }
            //
            var sourceIri = iriToIri[conFacade.getSource(connection)];
            var targetIri = iriToIri[conFacade.getTarget(connection)];
            var sourceId = this.data.iriToId[sourceIri];
            var targetId = this.data.iriToId[targetIri];
            // Ignore invalid connections.
            if (typeof (sourceId) === 'undefined' ||
                    typeof (targetId) === 'undefined') {
                console.warn('Ignored invalid connection.', connection);
                return;
            }
            // Add connection to the view.
            var cell = this.createControl(
                    sourceId,
                    targetId,
                    vertices);
            cellsToAdd.push(cell);
            var id = cell.id;
            // Add connection to the model.
            var newConnection = conFacade.createRunAfter(
                    this.pipeline);
            conFacade.setIriFromId(this.pipeline, newConnection, id);
            conFacade.setSource(newConnection,
                    this.data.idToResource[sourceId],
                    conFacade.getSourceBinding(connection));
            conFacade.setTarget(newConnection,
                    this.data.idToResource[targetId],
                    conFacade.getTargetBinding(connection));

            this.data.idToResource[id] = newConnection;
            this.data.iriToId[newConnection['@id']] = id;
        }.bind(this));

        this.canvas.getGraph().addCells(cellsToAdd);

        this.loadEnd();
    };

    /**
     * Return current pipeline in form of a JSON.
     */
    service.storePipeline = function () {
        var conFacade = this.pipelineModel.connection;
        this.canvas.getGraph().getLinks().forEach(function (cell) {
            var connection = this.getResource(cell.id);
            var verticesView = cell.attributes.vertices;
            if (verticesView) {
                var verticies = [];
                verticesView.forEach(function (item) {
                    verticies.push(conFacade.createVertex(
                            item['x'], item['y']));
                });
                conFacade.setVertices(this.pipeline, connection, verticies);
            } else {
                conFacade.setVertices(this.pipeline, connection, []);
            }
        }.bind(this));
        return this.pipelineModel.toJsonLd(this.pipeline);
    };

    /**
     * Return element for resource with given IRI.
     */
    service.getCell = function (iri) {
        var id = this.data.iriToId[iri];
        if (id === undefined) {
            return;
        }
        return this.canvas.getGraph().getCell(id);
    };

    /**
     * Return resource that is represented by cell with given ID.
     */
    service.getResource = function (id) {
        // TODO Remove idToResource and introduce typed dictionaries
        return this.data.idToResource[id];
    };

    service.getComponent = function (id) {
        var resource = this.data.idToResource[id];
        if (resource === undefined) {
            return undefined;
        }
        // Chack that the resource is a component.
        if (this.data.components[resource['@id']]) {
            return resource;
        } else {
            return undefined;
        }
    };

    /**
     * Remove cell with given ID from graph.
     */
    service.delete = function (id) {
        this.canvas.getGraph().getCell(id).remove();
    };

    service.onDelete = function (cell) {
        var resource = this.data.idToResource[cell.id];
        if (resource === undefined) {
            return;
        }
        // Notify all, but only if we are deleting something.
        this.canvas.getPaper().trigger('lp:resource:remove', resource['@id']);
        // Remove references from maps.
        delete this.data.iriToId[resource['@id']];
        delete this.data.idToResource[cell.id];
        delete this.data.components[resource['@id']];
        // Remove from pipeline definition.
        this.pipelineModel.delete(this.pipeline, resource['@id']);
        // Check for configuration graph and remove it if
        // it exists.
        var iri = this.pipelineModel.getComponentConfigurationUri(resource);
        if (iri !== undefined) {
            this.pipelineModel.deleteGraph(this.pipeline, iri);
        }
    };

    service.onAdd = function (cell) {
        if (this.status.disableEvents) {
            return;
        }
        if (this.data.idToResource[cell.id] !== undefined) {
            return;
        }
        // If connection is added by interaction with ports,
        // it may not be registed.
        if (cell instanceof joint.dia.Link) {
            var connection;
            var type = cell.attributes.type;
            if (type === 'control') {
                connection = this.pipelineModel.connection.createRunAfter(
                        this.pipeline);
            } else if (type === 'link') {
                connection = this.pipelineModel.connection.createConnection(
                        this.pipeline);
            } else {
                console.error('Unknown connection type: ', type);
                return;
            }
            //
            this.pipelineModel.connection.setIriFromId(
                    this.pipeline, connection, cell.id);
            this.data.idToResource[cell.id] = connection;
            this.data.iriToId[connection['@id']] = cell.id;
            //
            var source = cell.attributes.source;
            var target = cell.attributes.target;
            this.onConnectionChange(cell,
                    source.id, source.port,
                    target.id, target.port);
        }
    };

    service.onConnectionChange = function (cell, source, sourcePort,
            target, targetPort) {
        var connection = this.getResource(cell.id);
        if (source !== undefined) {
            this.pipelineModel.connection.setSource(connection,
                    this.getResource(source), sourcePort);
        }
        if (target !== undefined) {
            this.pipelineModel.connection.setTarget(connection,
                    this.getResource(target), targetPort);
        }
    };

    service.onPositionChange = function (cell) {
        this.pipelineModel.component.setPosition(
                this.getResource(cell.id),
                cell.attributes.position.x,
                cell.attributes.position.y);
    };

    service.clone = function (id) {

        /**
         * Create and return unique componnet id, that is not presented in
         * $scope.data.idToModel.
         */
        var uuid = function () {
            // credit: http://stackoverflow.com/posts/2117523/revisions
            var result = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(
                    /[xy]/g, function (c) {
                        var r = Math.random() * 16 | 0;
                        var v = c === 'x' ? r : (r & 0x3 | 0x8);
                        return v.toString(16);
                    });
            // Test for unique solution.
            if (this.data.idToResource[result]) {
                return uuid();
            } else {
                return result;
            }
        }.bind(this);

        var component = this.data.idToResource[id];
        if (component === undefined) {
            return;
        }
        var comService = this.pipelineModel.component;
        // Createa clone.
        var newComponent = this.pipelineModel.cloneComponent(
                this.pipeline, component, uuid());
        var templateIri = comService.getTemplateIri(newComponent);
        var template = this.templates.getTemplate(templateIri);

        // TODO Aditiona of Component is same as loading pipeline
        //              -> introduce a function.

        var cell = this.createComponent(newComponent, template,
                this.pipelineModel.component);
        //
        var id = cell.id;
        this.data.idToResource[id] = newComponent;
        this.data.iriToId[newComponent['@id']] = id;
        this.data.components[newComponent['@id']] = true;
        //
        this.canvas.getGraph().addCell(cell);
    };

    service.getPipeline = function () {
        return this.pipeline;
    };

    service.bind = function (canvas) {
        canvas.setConnectionValidator(validateConnection);
        this.canvas = canvas;

        canvas.getGraph().on('remove', service.onDelete.bind(this));

        canvas.getGraph().on('add', service.onAdd.bind(this));

        canvas.getGraph().on('change:position',
                service.onPositionChange.bind(this));

        canvas.getGraph().on('change:source', function (cell) {
            var source = cell.attributes.source;
            this.onConnectionChange(cell,
                    source.id, source.port,
                    void 0, void 0);
        }.bind(this));

        canvas.getGraph().on('change:target', function (cell) {
            var target = cell.attributes.target;
            this.onConnectionChange(cell,
                    void 0, void 0,
                    target.id, target.port);
        }.bind(this));

        canvas.getPaper().on('lp:component:changed', function (iri, component) {
            var cell = this.getCell(iri);
            if (component === undefined) {
                component = this.getComponent(cell.id);
            }
            var comService = this.pipelineModel.component;
            var templateIri = comService.getTemplateIri(component);
            var template = this.templates.getTemplate(templateIri);
            this.updateComponent(cell, component, template, comService);
        }.bind(this));

    };

    function factory(templates, pipelineModel, statusService) {
        return jQuery.extend(service, {
            'templates': templates,
            'pipelineModel': pipelineModel,
            'statusService': statusService
        });
    }

    // TODO Embeed functions from canvasDirective and make this
    // the main acess point for the canvas.

    return function (app) {
        app.factory('canvas.pipeline', [
            'template.service',
            'components.pipelines.services.model',
            'services.status',
            factory]);
    };

});
