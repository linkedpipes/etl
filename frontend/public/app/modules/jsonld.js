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

    const select = function (data) {
        if (data.length > 0) {
            return data[0];
        } else {
            return undefined;
        }
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
     * Return an array of string values. Each item contains the
     * "@value" property. The "@language" property is optional.
     *
     * If no language tag is provided the value is stored "".
     *
     * @param resource
     * @param predicate
     * @returns An array.
     */
    const getStrings = function (resource, predicate) {
        const getStringValue = function (value) {
            if (value["@value"] === undefined) {
                return {
                    "@value": value
                };
            } else if (value["@lang"] === undefined) {
                return {
                    "@value": value["@value"]
                };
            } else {
                return {
                    "@value": value["@value"],
                    "@language": value["@language"]
                };
            }
        };
        //
        let value = resource[predicate];
        if (value === undefined) {
            return [];
        }
        const result = [];
        if (!Array.isArray(value)) {
            value = [value];
        }
        value.forEach((item) => {
            result.push(getStringValue(item));
        });
        return result;
    };

    /**
     * Set string value under given predicate.
     *
     * @param resource
     * @param predicate
     * @param value String or string object, array of both, undefined.
     * @param lang Optional.
     */
    const setStrings = function (resource, predicate, value) {
        if (value === undefined) {
            delete resource[predicate];
            return;
        }
        if (!Array.isArray(value)) {
            value = [value];
        }
        const resourceValue = [];
        value.forEach((item) => {
            if (item["@value"] === undefined) {
                resourceValue.push({
                    "@value": item
                });
            } else {
                resourceValue.push({
                    "@value": item["@value"],
                    "@language": item["@language"]
                });
            }
        });
        resource[predicate] = resourceValue;
    };

    /**
     * Return an array of object values.
     *
     * @param resource
     * @param predicate
     * @returns An array.
     */
    const getValues = function (resource, predicate) {
        let value = resource[predicate];
        if (value === undefined) {
            return [];
        }
        const result = [];
        if (!Array.isArray(value)) {
            value = [value];
        }
        value.forEach((item) => {
            // TODO Support @type ?
            if (item["@value"] === undefined) {
                result.push(item);
            } else {
                result.push(item["@value"]);
            }
        });
        return result;
    };

    /**
     * Set array of values under given property, the values may be
     * simple values of values JSON-LD objects (object with @value).
     *
     * @param resource
     * @param predicate
     * @param value
     */
    const setValues = function (resource, predicate, value) {
        if (value === undefined) {
            delete resource[predicate];
            return;
        }
        if (!Array.isArray(value)) {
            value = [value];
        }
        const resourceValue = [];
        value.forEach((item) => {
            if (item["@value"] === undefined) {
                resourceValue.push({
                    "@value": item
                });
            } else {
                resourceValue.push(item);
            }
        });
        resource[predicate] = resourceValue;
    };

    /**
     * Return an array of IRI values for given predicate.
     *
     * @param resource
     * @param predicate
     * @return An array.
     */
    const getIRIs = function (resource, predicate) {
        const getIRIValue = function (value, result) {
            if (value["@id"] !== undefined) {
                result.push(value["@id"]);
            } else if (value.id !== undefined) {
                result.push(value.id);
            } else if (value["@value"] !== undefined) {
                console.warn("IRI stored as value: ", value,
                    "on object", resource);
                result.push(value["@value"]);
            } else {
                console.error("Invalid IRI value: ", value,
                    "on object", resource);
            }
        };
        //
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

    /**
     * Set IRIs values under given predicate.
     *
     * @param resource
     * @param predicate
     * @param value
     */
    const setIRIs = function (resource, predicate, value) {
        if (value === undefined) {
            delete resource[predicate];
            return;
        }
        if (!Array.isArray(value)) {
            value = [value];
        }
        const resourceValue = [];
        value.forEach((item) => {
            if (item["@id"] === undefined) {
                resourceValue.push({
                    "@id": item
                });
            } else {
                resourceValue.push(item);
            }
        });
        resource[predicate] = resourceValue;
    };

    const getIntegers = function (resource, predicate) {
        const value = getValues(resource, predicate);
        const result = [];
        value.forEach((item) => {
            result.push(parseInt(item));
        });
        return result;
    };

    const setIntegers = function (resource, predicate, value) {
        setValues(resource, predicate, value);
    };

    const getBooleans = function (resource, predicate) {
        const value = getValues(resource, predicate);
        const result = [];
        value.forEach((item) => {
            if (item === "true") {
                result.push(true);
            } else if (item === "false") {
                result.push(false);
            } else {
                result.push(value);
            }
        });
        return result;
    };

    const setBooleans = function (resource, predicate, value) {
        setValues(resource, predicate, value);
    };

    const getDates = function (resource, predicate) {
        const value = getValues(resource, predicate);
        const result = [];
        value.forEach((item) => {
            result.push(new Date(item));
        });
        return result;
    };

    const setDates = function (resource, predicate, value) {
        if (value === undefined) {
            delete resource[predicate];
            return;
        }
        if (!Array.isArray(value)) {
            value = [value];
        }
        const resourceValue = [];
        value.forEach((item) => {
            let valueAsString = item.getFullYear() + '-';
            if (value.getMonth() + 1 < 10) {
                valueAsString += '0';
            }
            valueAsString += (item.getMonth() + 1) + '-';
            if (value.getDate() < 10) {
                valueAsString += '0';
            }
            valueAsString += item.getDate();
            resourceValue.push({
                "@value": valueAsString
            });
        });
        resource[predicate] = resourceValue;
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
        "getStrings": getStrings,
        "getString": (resource, predicate) =>
            select(getStrings(resource, predicate)),
        "setStrings": setStrings,
        "getIntegers": getIntegers,
        "getInteger": (resource, predicate) =>
            select(getIntegers(resource, predicate)),
        "setIntegers": setIntegers,
        "getBooleans": getBooleans,
        "getBoolean": (resource, predicate) =>
            select(getBooleans(resource, predicate)),
        "setBooleans": setBooleans,
        "getDates": getDates,
        "getDate": (resource, predicate) =>
            select(getDates(resource, predicate)),
        "setDates": setDates,
        "getIRIs": getIRIs,
        "getIRI": (resource, predicate) =>
            select(getIRIs(resource, predicate)),
        "setIRIs": setIRIs,
        "getReferences": getReferences
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
