((definition) => {
    if (typeof define === "function" && define.amd) {
        define([
            "jquery",
            "jsonld",
            "vocabulary",
            "./component-model",
            "./connection-model",
            "./execution-model"
        ], definition);
    }
})((jQuery, jsonld, vocabulary,
    componentModel, connectionModel, executionModel) => {

    const LP = vocabulary.LP;

    const SKOS = vocabulary.SKOS;

    const DCTERMS = vocabulary.DCTERMS;

    const service = {
        "component": componentModel,
        "connection": connectionModel,
        "execution": executionModel
    };

    service.createFromJsonLd = (data) => {

        const model = {
            "graphs": {},
            "pipeline": {
                "iri": undefined,
                "graph": undefined,
                "resource": undefined
            },
            "iris": new Set()
        };

        jsonld.q.iterateResources(data, (resource, iri) => {
            model.iris.add(resource["@id"]);
            const types = jsonld.r.getTypes(resource);
            if (types.indexOf(LP.PIPELINE) !== -1) {
                model.pipeline.graph = jsonld.q.getGraph(data, iri);
                model.pipeline.iri = iri;
                model.pipeline.resource = resource;
                return true;
            }
        });

        jsonld.q.iterateGraphs(data, (graph, iri) => {
            model.graphs[iri] = graph;
        });

        return model;
    };

    service.getPipelineLabel = (model) => {
        return jsonld.r.getPlainString(
            model.pipeline.resource, SKOS.PREF_LABEL);
    };

    service.getPipelineResource = (model) => {
        return model.pipeline.resource;
    };

    service.getComponents = (model) => {
        return getByType(model, LP.COMPONENT);
    };

    function getByType(model, type) {
        const output = [];
        const graph = getPipelineGraph(model);
        jsonld.t.iterateResources(graph, (resource) => {
            if (resource === undefined) {
                return;
            }
            const types = jsonld.r.getTypes(resource);
            if (types.indexOf(type) !== -1) {
                output.push(resource);
            }
        });
        return output;
    }

    service.getDataLinks = (model) => {
        return getByType(model, LP.CONNECTION);
    };

    service.getRunAfter = (model) => {
        return getByType(model, LP.RUN_AFTER);
    };

    service.createComponent = (model, template, x, y) => {
        const component = {
            "@id": createIri(model, "component"),
            "@type": [LP.COMPONENT],
            [SKOS.PREF_LABEL]: template.label,
            [LP.HAS_X]: x,
            [LP.HAS_Y]: y,
            [LP.HAS_TEMPLATE]: {
                "@id": template.id
            }
        };
        getPipelineGraph(model).push(component);
        return component;
    };

    function createIri(model, type) {
        let iri = generateIri(model, type);
        while (model.iris.has(iri)) {
            iri = generateIri(model, type)
        }
        model.iris.add(iri);
        return iri;
    }

    function generateIri(model, type) {
        // TODO Prefix with time.
        const id = "xxxx-yxxx".replace(
            /[xy]/g, (c) => {
                const r = Math.random() * 16 | 0;
                const v = c === "x" ? r : (r & 0x3 | 0x8);
                return v.toString(16);
            });
        return model.pipeline.iri + "/" + type + "/" + id.toLowerCase();
    }

    function getPipelineGraph(model) {
        return model.pipeline.graph;
    }

    service.createDataLink = (model) => {
        const connection = {
            "@id": createIri(model, "connection"),
            "@type": [LP.CONNECTION]
        };
        getPipelineGraph(model).push(connection);
        return connection;
    };

    service.createRunAfterLink = (model) => {
        const connection = {
            "@id": createIri(model, "connection"),
            "@type": [LP.RUN_AFTER]
        };
        getPipelineGraph(model).push(connection);
        return connection;
    };

    service.createVertex = (iri, x, y, order) => {
        return {
            "@id": iri,
            "@type": [LP.VERTEX],
            [LP.HAS_X]: x,
            [LP.HAS_Y]: y,
            [LP.HAS_ORDER]: order
        }
    };

    service.getVertices = (model, connection) => {
        const graph = getPipelineGraph(model);
        return jsonld.r.getIRIs(connection, LP.HAS_VERTEX)
            .map((iri) => jsonld.t.getResource(graph, iri));
    };

    service.getComponentConfiguration = (model, component) => {
        const iri = jsonld.r.getIRI(component, LP.HAS_CONFIGURATION);
        if (iri === undefined) {
            return undefined;
        }
        return model.graphs[iri];
    };

    service.setComponentConfiguration = (model, component, configuration) => {
        let iri = jsonld.r.getIRI(component, LP.HAS_CONFIGURATION);
        if (iri === undefined) {
            iri = jsonld.r.getId(component) + "/configuration";
            jsonld.r.setIRIs(component, LP.HAS_CONFIGURATION, iri);
        }
        model.graphs[iri] = configuration;
    };

    service.getExecutionProfile = (model) => {
        const iri = jsonld.r.getIRI(
            model.pipeline.resource, LP.HAS_EXECUTION_PROFILE);
        if (iri) {
            return jsonld.t.getResource(getPipelineGraph(model), iri);
        } else {
            const profile = createExecutionProfile(model);
            jsonld.r.setIRIs(
                model.pipeline.resource,
                LP.HAS_EXECUTION_PROFILE,
                profile["@id"]);
            getPipelineGraph(model).push(profile);
            return profile;
        }
    };

    function createExecutionProfile(model) {
        const iri = model.pipeline.iri + "/profile/default";
        const profile = {
            "@id": iri,
            "@type": [LP.EXECUTION_PROFILE],
            [LP.HAS_REPO_POLICY]: {
                "@id": LP.RDF_SINGLE_REPOSITORY
            }
        };
        getPipelineGraph(model).push(profile);
        return profile;
    }

    service.setVertices = (model, connection, vertices) => {
        jsonld.r.getIRIs(connection, LP.HAS_VERTEX).forEach((iri) => {
            service.deleteByIri(model, iri);
        });
        if (vertices === undefined || vertices.length === 0) {
            connection[LP.HAS_VERTEX] = [];
            return;
        }
        // Add new.
        const graph = getPipelineGraph(model);
        const resources = [];
        let order = 0;
        vertices.forEach((vertex) => {
            ++order;
            const iri = createIri(model, "vertex");
            const resource = service.createVertex(
                iri, vertex.x, vertex.y, order);
            graph.push(resource);
            resources.push(resource);
        });
        connection[LP.HAS_VERTEX] = resources
            .map((resource) => ({"@id": jsonld.r.getId(resource)}));
    };

    service.deleteByIri = (model, iri) => {
        const graph = getPipelineGraph(model);
        for (let index in graph) {
            if (!graph.hasOwnProperty(index)) {
                continue;
            }
            const resource = graph[index];
            if (resource === undefined) {
                // The resource may have been deleted.
                continue;
            }
            if (jsonld.r.getId(resource) !== iri) {
                continue;
            }
            graph[index] = undefined;
            // Additional clean up.
            const types = jsonld.r.getTypes(resource);
            if (types.indexOf(LP.RUN_AFTER) !== -1 ||
                types.indexOf(LP.CONNECTION) !== -1) {
                // Delete vertices.
                jsonld.r.getIRIs(resource, LP.HAS_VERTEX).forEach((iri) => {
                    service.deleteByIri(model, iri);
                });
            }
            if (types.indexOf(LP.COMPONENT) !== -1) {
                const configuration =
                    jsonld.r.getIRI(resource, LP.HAS_CONFIGURATION);
                if (configuration) {
                    model.graphs[configuration] = undefined;
                }
            }
            break;
        }
    };

    service.getResource = (model, iri) => {
        const graph = getPipelineGraph(model);
        for (let index in graph) {
            if (!graph.hasOwnProperty(index)) {
                continue;
            }
            if (graph[index] === undefined) {
                continue;
            }
            if (jsonld.r.getId(graph[index]) === iri) {
                return graph[index];
            }
        }
        return undefined;
    };

    service.setResource = (model, resource) => {
        const iri = jsonld.r.getId(resource);
        const graph = getPipelineGraph(model);
        for (let index in graph) {
            if (!graph.hasOwnProperty(index)) {
                continue;
            }
            if (graph[index] === undefined) {
                continue;
            }
            if (jsonld.r.getId(graph[index]) === iri) {
                graph[index] = resource;
                return;
            }
        }
        graph.push(resource);
    };

    service.asJsonLd = (model) => {
        shakeOffPipelineGraph(model);
        const output = [];
        for (let key in model.graphs) {
            if (!model.graphs.hasOwnProperty(key)) {
                continue;
            }
            output.push({
                "@id": key,
                "@graph": model.graphs[key]
            });
        }
        return output;
    };

    /**
     * Remove undefined and null objects in configuration graph.
     */
    function shakeOffPipelineGraph(model) {
        console.time("shakeOffPipelineGraph");
        const target = [];
        getPipelineGraph(model).forEach((value) => {
            if (value === undefined || value === null) {
                return;
            }
            target.push(value);
        });
        model.pipeline.graph = target;
        model.graphs[model.pipeline.iri] = target;
        console.timeEnd("shakeOffPipelineGraph");
    }

    service.cloneComponent = (model, component) => {
        // Clone a component.
        const clone = jQuery.extend(true, {}, component);
        clone["@id"] = createIri(model, "component");
        getPipelineGraph(model).push(clone);
        // Move the component - so they are not on the same position.
        service.component.setPosition(
            clone,
            service.component.getX(component),
            // TODO Do not use fixed value here.
            service.component.getY(component) + 100
        );
        // Copy configuration.
        const configuration = service.getComponentConfiguration(model, component);
        if (configuration === undefined) {
            return clone;
        }
        const configurationGraph = clone["@id"] + "/configuration";
        const configurationClone = cloneConfigurationGraph(configuration);
        jsonld.r.setIRIs(clone, LP.HAS_CONFIGURATION, configurationGraph);
        service.setComponentConfiguration(model, clone, configurationClone);
        return clone;
    };

    function cloneConfigurationGraph(configuration) {
        const result = [];
        configuration.forEach((item) => {
            result.push(jQuery.extend(true, {}, item));
        });
        return result;
    }

    /**
     * Consume given model to add.
     *
     * TODO Extract to another function?
     * TODO Add test with adding it self.
     */
    service.addPipeline = (model, x, y, modelToAdd) => {
        const components = getByType(modelToAdd, LP.COMPONENT);
        const dataLinks = getByType(modelToAdd, LP.CONNECTION);
        const runAfterLinks = getByType(modelToAdd, LP.RUN_AFTER);
        const vertices = getByType(modelToAdd, LP.VERTEX);

        // Update position, so the pipeline is on the place where user want it.
        const {"x": minX, "y": minY} = findLeftMostComponent(components);
        transformCoordinates(modelToAdd, -minX + x, -minY + y);

        // Update IRIs to be compatible with model.
        updateIris(model, modelToAdd);

        // Add all resources from modelToAdd to model.
        model.pipeline.graph = model.pipeline.graph.concat(
            components, dataLinks, runAfterLinks, vertices);
        model.graphs[model.pipeline.iri] = model.pipeline.graph;

        // Add configuration graphs from modelToAdd to model.
        components.forEach((component) => {
            const iri = jsonld.r.getIRI(component, LP.HAS_CONFIGURATION);
            const configuration = modelToAdd.graphs[iri];
            console.log(iri, configuration);
            if (!configuration) {
                return;
            }
            model.graphs[iri] = configuration;
        });

        return {
            "components": components,
            "dataLinks": dataLinks,
            "runAfterLinks": runAfterLinks
        };
    };

    function findLeftMostComponent(components) {
        let x = Number.POSITIVE_INFINITY;
        let y = Number.POSITIVE_INFINITY;
        components.forEach((component) => {
            x = Math.min(x, componentModel.getX(component));
            y = Math.min(y, componentModel.getY(component));
        });
        return {"x": x, "y": y};
    }

    function transformCoordinates(model, moveOnX, moveOnY) {
        const graph = getPipelineGraph(model);
        jsonld.t.iterateResources(graph, (resource) => {
            if (resource === undefined) {
                return;
            }
            const x = jsonld.r.getInteger(resource, LP.HAS_X);
            if (x) {
                jsonld.r.setIntegers(resource, LP.HAS_X, x + moveOnX);
            }
            const y = jsonld.r.getInteger(resource, LP.HAS_Y);
            if (y) {
                jsonld.r.setIntegers(resource, LP.HAS_Y, y + moveOnY);
            }
        });
    }

    function updateIris(model, modelToAdd) {
        const iriMapping = {};

        // Collect IRIs from a pipeline.
        const base = modelToAdd.pipeline.iri;
        getPipelineGraph(modelToAdd).forEach((resource) => {
            const iri = jsonld.r.getId(resource);
            if (!iri.startsWith(base)) {
                // Update only those that are in scope of a pipeline.
                return;
            }
            const type = getIriType(resource);
            const newIri = createIri(model, type);
            iriMapping[iri] = newIri;
            // Check for configurations.
            if (jsonld.r.getTypes(resource).includes(LP.COMPONENT)) {
                const configuration = jsonld.r.getIRI(
                    resource, LP.HAS_CONFIGURATION);
                iriMapping[configuration] = newIri + "/configuration";
            }
        });

        // Update references in objects.
        getPipelineGraph(modelToAdd).forEach((resource) => {
            callForEveryObjectWithProperty(resource, "@id", (object) => {
                const newId = iriMapping[jsonld.r.getId(object)];
                if (newId) {
                    jsonld.r.setId(object, newId);
                }
            });
        });

        // Update graphs.
        const graphs = {};
        for (let key in modelToAdd.graphs) {
            const newKey = iriMapping[key];
            if (newKey) {
                graphs[newKey] = modelToAdd.graphs[key];
            } else {
                graphs[key] = modelToAdd.graphs[key];
            }
        }

        modelToAdd.graphs = graphs;
    }

    function getIriType(resource) {
        const types = jsonld.r.getTypes(resource);
        if (types.includes(LP.COMPONENT)) {
            return "component";
        } else if (types.includes(LP.CONNECTION)) {
            return "connection";
        } else if (types.includes(LP.RUN_AFTER)) {
            return "connection";
        } else if (types.includes(LP.VERTEX)) {
            return "vertex";
        } else {
            return "data";
        }
    }

    function callForEveryObjectWithProperty(object, property, callback) {
        if (!object) {
            // Primitive value.
            return;
        } else if (Array.isArray(object)) {
            object.forEach((item) => callForEveryObjectWithProperty(
                item, property, callback));
        } else if (typeof object === "object") {
            if (object[property] !== undefined) {
                callback(object);
            }
            for (let key in object) {
                if (!object.hasOwnProperty(key)) {
                    return;
                }
                callForEveryObjectWithProperty(
                    object[key], property, callback);
            }
        }
    }

    service.doConnectionExists = (
        model, componentS, portS, componentT, portT) => {
        // TODO Add connection map to speed up.
        const connections = getByType(model, LP.CONNECTION);

        const iriS = jsonld.r.getId(componentS);
        const iriT = jsonld.r.getId(componentT);

        for (let index in connections) {
            const connection = connections[index];
            if (connectionModel.getSource(connection) !== iriS) {
                continue;
            }
            if (connectionModel.getTarget(connection) !== iriT) {
                continue;
            }
            if (connectionModel.getSourceBinding(connection) !== portS) {
                continue;
            }
            if (connectionModel.getTargetBinding(connection) !== portT) {
                continue;
            }
            return true;
        }
        return false;
    };

    service.isDataLink = (resource) => {
        const types = jsonld.r.getTypes(resource);
        return types.indexOf(LP.CONNECTION) > -1;
    };

    service.isComponent = (resource) => {
        const types = jsonld.r.getTypes(resource);
        return types.indexOf(LP.COMPONENT) > -1;
    };


    return service;
});