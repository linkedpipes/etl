/**
 * Library for R/W manipulation with JSON-LD.
 *
 * @author Petr Škoda
 */

(function () {
    "use strict";

    /**
     * Given the input data normalize them. The output form is:
     * {
     *  "@graph" : [
     *      "@graph" : {GRAPH DATA},
     *      "@id" : {OPTIONAL GRAPH IRI}
     *  ]
     * }
     *
     * @param data
     * @return Normalized form of the data.
     */
    const normalizeData = function (data) {
        var graphList = [];

        if (data["@graph"]) {
            if (data["@graph"].length === 0) {
                // Empty.
                return {
                    "@graph": []
                };
            } else {
                // There is at least one element.
                if (data["@graph"][0]["@graph"]) {
                    // The data are in form we need.
                    return data;
                } else {
                    // There is only one graph.
                    graphList.push(data);
                }
            }
        } else if (Array.isArray(data)) {
            // It"s array with graphs.
            return {
                "@graph": data
            };
        } else {
            // There is no graph.
            graphList.push({
                "@graph": data
            });
        }
        //
        return {
            "@graph": graphList
        };
    };

    //
    // Manipulation with resources.
    //

    const getId = function (value) {
        if (value["@id"]) {
            return value["@id"];
        } else if (value.id) {
            return value.id;
        } else {
            // Missing reference - ie. blank node.
            return null;
        }
    };

    /**
     *
     * @param resource
     * @returns Array of types or empty collection.
     */
    const getTypes = function (resource) {
        if (resource["@type"]) {
            return resource ["@type"];
        } else {
            return [];
        }
    };

    /**
     * Return a dictionary where under language tags the string values
     * are stored.
     *
     * @param resource
     * @param predicate
     * @returns Dictionary.
     */
    const getString = function (resource, predicate) {

        const getStringValue = function (value, result) {
            if (value["@value"] === undefined) {
                result[""] = value;
            } else if (value["@lang"] === undefined) {
                result[""] = value["@value"];
            } else {
                result[value["@lang"]] = value["@value"];
            }
        };

        const value = resource[predicate];
        if (value === undefined) {
            return {};
        }

        const result = {};
        if (Array.isArray(value)) {
            if (value.length === 0) {
                return result;
            } else if (value.length === 1) {
                getStringValue(value[0], result);
            } else {
                getStringValue(value[0], result);
                console.log("Only single value used for ", value, "on",
                    resource);
            }
        } else {
            getStringValue(value, result);
        }
        return result;
    };

    const getStrings = function (resource, predicate) {

        const value = resource[predicate];
        if (value === undefined) {
            return [];
        }

        if (Array.isArray(value)) {
            const results = [];
            for (var index in value) {
                if (!value.hasOwnProperty(index)) {
                    continue;
                }
                results.push(getString(value, index));
            }
            return results;
        } else {
            return [getString(resource, predicate)];
        }
    };

    /**
     * Return a value under given predicate. In case of multiple values
     * only one is returned.
     *
     * @param resource
     * @param predicate
     * @returns Single value or undefined.
     */
    const getValue = function (resource, predicate) {
        let value = resource[predicate];
        if (value === undefined) {
            return {};
        }

        if (Array.isArray(value)) {
            value = value[0];
        }

        if (value["@value"] === undefined) {
            return value;
        } else {
            return value["@value"];
        }
    };

    /**
     * Return an IRI value for given predicate. If multiple are
     * presented only one is used.
     *
     * @param resource
     * @param predicate
     * @return A single value.
     */
    const getIRIs = function (resource, predicate) {

        const getIRIValue = function (value, result) {
            if (value["@id"] !== undefined) {
                result.push(value["@id"]);
            } else if (value.id !== undefined) {
                result.push(value.id);
            } else {
                console.error("Invalid IRI reference: ", value, "on", resource);
            }
        };

        const value = resource[predicate];
        if (value === undefined) {
            return [];
        }

        const result = [];
        if (Array.isArray(value)) {
            for (var itemIndex in value) {
                if (!value.hasOwnProperty(itemIndex)) {
                    continue;
                }
                getIRIValue(value[itemIndex], result);
            }
        } else {
            getIRIValue(value, result);
        }
        return result;
    };

    const getIRI = function (resource, predicate) {
        const result = getIRIs(resource, predicate);
        if (result.length > 0) {
            return result[0];
        } else {
            return undefined;
        }
    }

    /**
     * Set string value under given predicate.
     *
     * @param resource
     * @param predicate
     * @param value String or string object.
     * @param lang Optional.
     */
    const setString = function (resource, predicate, value, lang) {
        if (value === undefined) {
            delete resource[predicate];
            return;
        }
        // Check for object.
        if (Array.isArray(value) || value["@value"] !== undefined) {
            resource[predicate] = value;
            return;
        }
        //
        if (lang !== undefined || lang === "") {
            resource[predicate] = {
                "@value" : value,
                "@language" : lang
            };
        } else {
            resource[predicate] = {
                "@value" : value,
            };
        }
    };

    /**
     * Set general type value under given predicate.
     *
     * @param resource
     * @param predicate
     * @param value
     */
    const setValue = function (resource, predicate, value) {
        if (value === undefined) {
            delete resource[predicate];
            return;
        }
        resource[predicate] = {
            "@value" : value,
        };
    };

    /**
     * Set IRI value under given predicate.
     *
     * @param resource
     * @param predicate
     * @param value
     */
    const setIRI = function (resource, predicate, value) {
        if (value === undefined) {
            delete resource[predicate];
            return;
        }
        resource[predicate] = {
            "@id" : value,
        };
    };

    //
    // Manipulation with triples.
    //

    /**
     * Iterate over resources in given graph. The data object must contains
     * the graph and graphIri.
     *
     * If callback returns something else than undefined and false then stops
     * the iteration and return what callback returned.
     *
     * @param data Data object.
     * @param callback
     * @returns
     */
    const iterateResources = function (data, callback) {
        for (let index in data.graph) {
            if (!data.graph.hasOwnProperty(index)) {
                continue;
            }
            const resource = data.graph[index];
            const result = callback(resource, data.graphIri);
            if (result) {
                return result;
            }
        }
        return false;
    };

    /**
     * Return a list of referenced object.
     *
     * @param data
     * @param resource
     * @param predicate
     * @return Array.
     */
    const getReferences = function (data, resource, predicate) {
        const results = [];
        const iris = getIRIs(resource, predicate);
        data._data.graph.forEach((resource) => {
            if (iris.indexOf(getId(resource)) !== -1) {
                results.push(resource);
            }
        });
        return results;
    };

    /**
     * Return a single resource with given IRI.
     *
     * @param data
     * @param ref
     */
    const getResource = function (data, ref) {
        for (let index in data.graph) {
            if (!data.graph.hasOwnProperty(index)) {
                continue;
            }
            const resource = data.graph[index];
            if (getId(resource) === ref.resource) {
                return resource;
            }
        }
    };

    //
    // Graph manipulation.
    //

    /**
     * Find and return content of graph with given IRI.
     *
     * @param data Normalized JSONLD data.
     * @param iri
     * @returns undefined if no graph of given name exists.
     */
    const getGraph = function (data, iri) {
        const graphs = data["@graph"];
        for (let index in graphs) {
            if (!graphs.hasOwnProperty(index)) {
                continue;
            }
            const graph = graphs[index];
            if (graph["@id"] === iri) {
                return graph["@graph"];
            }
        }
        return undefined;
    };

    /**
     * Create new graph of given IRI. If graph already exists does nothing.
     *
     * @param data Normalized JSONLD data.
     * @param iri
     * @param Graph content, optional argument.
     */
    const createGraph = function (data, iri, content) {
        const graphs = data["@graph"];
        // Check existence.
        if (getGraph(data, iri) !== undefined) {
            return;
        }
        // If no content is given use empty object.
        if (content === undefined) {
            content = {};
        }
        //
        graphs.push({
            "@graph": content,
            "@id": iri
        });
    };

    /**
     * Find and delete graph with given content.
     *
     * @param data Normalized JSONLD data.
     * @param iri
     */
    const deleteGraph = function (data, iri) {
        const graphs = data["@graph"];
        for (let index in graphs) {
            if (!graphs.hasOwnProperty(index)) {
                continue;
            }
            const graph = graphs[index];
            if (graph["@id"] === iri) {
                delete graphs[index];
                return;
            }
        }
    };

    /**
     * Iterate all graphs, for each graph call callback. If callback returns
     * something else than undefined and false then stops the iteration and
     * return what callback returned.
     *
     * @param data Normalized JSONLD data.
     * @param callback
     */
    const iterateGraphs = function (data, callback) {
        const graphs = data["@graph"];
        for (let index in graphs) {
            if (!graphs.hasOwnProperty(index)) {
                continue;
            }
            const graph = graphs[index];
            const result = callback(graph["@graph"], graph["@id"]);
            if (result) {
                return result;
            }
        }
        return false;
    };

    /**
     * Create and return new object for manipulation with
     * triples on given graph.
     *
     * @param data Normalized JSONLD data.
     * @param status Internal status.
     * @param iri
     * @param graph Optional, if given iri is ignored.
     * @return This for chaining.
     */
    const graph = function (data, iri, graph) {
        if (graph === undefined) {
            return new triplePrototype(getGraph(data, iri), iri);
        } else {
            return new triplePrototype(graph, iri);
        }
    };

    /**
     * Iterate over all resources in all graphs.
     *
     * The callback is given the graph data and the graph IRI as a second
     * argument. If callback returns something else than undefined and false
     * then stops the iteration and return what callback returned.
     *
     * @param data
     * @param callback
     */
    const iterateResourcesInGraphs = function (data, callback) {
        return iterateGraphs(data, (graph, iri) => {
            return iterateResources({"graph": graph, "graphIri": iri},
                callback);
        });
    };

    /**
     * Search for a resource of given type. Return reference to the first
     * resource that match the given type.
     *
     * The reference contains IRI of the graph and resource.
     *
     * @param data
     * @param type
     */
    const findByType = function (data, type) {
        return iterateResourcesInGraphs(data, (resource, iri) => {
            var types = getTypes(resource);
            if (types.indexOf(type) !== -1) {
                return {
                    "resource": getId(resource),
                    "graph": iri
                };
            }
        });
    };

    //
    // Declaration of main service prototype and API export.
    //

    const resourceService = {
        "getTypes": getTypes,
        "getId": getId,
        "getString": getString,
        "getStrings": getStrings,
        "getInteger" : (resource, predicate) => {
            const value = getValue(resource, predicate);
            if (value === undefined) {
                return value;
            } else {
                return parseInt(value);
            }
        },
        "getBoolean" : getValue,
        "getIRIs": getIRIs,
        "getIRI": getIRI,
        "getReferences": getReferences,
        "setString" : setString,
        "setInteger" : setValue,
        "setBoolean" : setValue,
        "setIRI" : setIRI
    };

    /* jshint latedef: false */
    const triplePrototype = function (graph, iri) {
        this._data = {
            "graph": graph,
            "graphIri": iri
        };

        this.iterate = iterateResources.bind(null, this._data);
        this.findByType = (type) => {
            // findByType works with quads so we wrap the triples.
            const wrap = {
                "@graph": [
                    {
                        "@graph": this._data.graph,
                        "@id": this._data.graphIri
                    }
                ]
            };
            return findByType(wrap, type);
        };
        this.getResource = getResource.bind(null, this._data)

        return this;
    };

    const quadsPrototype = function (data) {
        // Prepare data and status objects.
        this._data = normalizeData(data);
        this._status = {};

        this.createGraph = createGraph.bind(null, this._data);
        this.deleteGraph = deleteGraph.bind(null, this._data);
        this.iterateGraphs = iterateGraphs.bind(null, this._data);
        this.selectGraph = graph.bind(null, this._data);
        this.iterateResources = iterateResourcesInGraphs.bind(null, this._data);
        this.findByType = findByType.bind(null, this._data);

        this.asJsonLd = function () {
            return this._data;
        };

    };

    var factory = {
        "quads": (data) => {
            return new quadsPrototype(data);
        },
        "triples": (graph, iri) => {
            if (iri === undefined && graph["@graph"] && graph["@id"]) {
                iri = graph["@id"];
            }
            if (graph["@graph"]) {
                graph = graph["@graph"];
            }
            return new triplePrototype(graph, iri);
        },
        "r": resourceService
    };

    if (typeof define === "function" && define.amd) {
        define([], function () {
            return factory;
        });
    }

})();
