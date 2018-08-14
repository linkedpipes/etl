/**
 * Library for R/W manipulation with JSON-LD.
 */
((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    } else if (typeof module !== "undefined") {
        module.exports = definition();
    }
})(() => {
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
        const graphList = [];

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
            data.forEach((item) => {
                if (item["@graph"] !== undefined) {
                    graphList.push(item);
                } else {
                    // Default graph.
                    if (!Array.isArray(item)) {
                        // It can be a single object.
                        item = [item];
                    }
                    graphList.push({"@graph": item, "@id": ""});
                }
            });
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
            } else if (value["@language"] === undefined) {
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
        if (value === undefined || value === null) {
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
                const lang = item["@language"];
                if (lang === undefined || lang === "") {
                    resourceValue.push({
                        "@value": item["@value"]
                    });
                } else {
                    resourceValue.push({
                        "@value": item["@value"],
                        "@language": item["@language"]
                    });
                }
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
        if (value === undefined || value === null) {
            delete resource[predicate];
            return;
        }
        value = asArray(value);
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

    const asArray = function (value) {
        if (value === undefined || value === null) {
            return [];
        }
        if (!Array.isArray(value)) {
            return [value];
        } else {
            return value;
        }
    }

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
            for (let itemIndex in value) {
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
        if (value === undefined || value === '') {
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
        let filteredValues = asArray(value).filter((x) => x !== "");
        setValues(resource, predicate, filteredValues);
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
                result.push(item);
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
            if (item.getMonth() + 1 < 10) {
                valueAsString += '0';
            }
            valueAsString += (item.getMonth() + 1) + '-';
            if (item.getDate() < 10) {
                valueAsString += '0';
            }
            valueAsString += item.getDate();
            resourceValue.push({
                "@value": valueAsString
            });
        });
        resource[predicate] = resourceValue;
    };

    /**
     * Return whole value object as stored under the property.
     */
    const getRawValue = function (resource, predicate) {
        return resource[predicate];
    };

    const setRawValue = function (resource, predicate, value) {
        if (value === undefined) {
            delete resource[predicate];
        } else {
            resource[predicate] = value;
        }
    };

    /**
     * Iterate over resources in given graph.
     *
     * @param data Data object.
     * @param callback
     * @returns
     */
    const iterateResources = function (graph, callback) {
        for (let index in graph) {
            if (!graph.hasOwnProperty(index)) {
                continue;
            }
            const resource = graph[index];
            const result = callback(resource);
            if (result !== undefined) {
                return result;
            }
        }
        return undefined;
    };

    /**
     * Return a list of referenced object.
     *
     * @param graph
     * @param resource
     * @param predicate
     * @return Array.
     */
    const getReferences = function (graph, resource, predicate) {
        const results = [];
        const iris = getIRIs(resource, predicate);
        graph.forEach((resource) => {
            if (iris.indexOf(getId(resource)) !== -1) {
                results.push(resource);
            }
        });
        return results;
    };

    /**
     * Find and return content of graph with given IRI.
     *
     * @param data Normalized JSONLD data.
     * @param iri If null return first graph.
     * @returns undefined if no graph of given name exists.
     */
    const getGraph = function (data, iri) {
        const graphs = data["@graph"];
        if (iri === null) {
            return graphs[0]["@graph"];
        }
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
            return iterateResources(graph,
                (resource) => callback(resource, iri)
            );
        });
    };

    const resourceService = {
        "getTypes": getTypes,
        "getId": getId,
        "getStrings": getStrings,
        "getString": (resource, predicate) =>
            select(getStrings(resource, predicate)),
        "getPlainString": (resource, predicate) => {
            const value = select(getStrings(resource, predicate));
            if (value === undefined) {
                return undefined;
            } else {
                return value["@value"];
            }
        },
        "getPlainStrings": (resource, predicate) => {
            const values = getStrings(resource, predicate);
            if (values === undefined) {
                return undefined;
            }
            return values.map((item) => item["@value"]);

        },
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
        "getValue": getRawValue,
        "setValue": setRawValue
    };

    const triplesService = {
        "iterateResources": iterateResources,
        "getResource": (graph, iri) => {
            return iterateResources(graph, (resource) => {
                if (getId(resource) === iri) {
                    return resource;
                }
            });
        },
        "getResourceByType": (graph, type) => {
            return iterateResources(graph, (resource) => {
                const types = getTypes(resource);
                if (types.indexOf(type) !== -1) {
                    return resource;
                }
            });
        },
        "getReferences": getReferences
    };

    // TODO Normalize data only once before first use.
    const quadsService = {
        "getGraph": (data, iri) => {
            const normalized = normalizeData(data);
            return getGraph(normalized, iri);
        },
        "iterateGraphs": (data, callback) => {
            const normalized = normalizeData(data);
            return iterateGraphs(normalized, callback);
        },
        "iterateResources": (data, callback) => {
            const normalized = normalizeData(data);
            return iterateResourcesInGraphs(normalized, callback);
        },
        "getResource": (data, graphIri, iri) => {
            const normalized = normalizeData(data);
            const graph = getGraph(normalized, graphIri);
            return iterateResources(graph, (resource) => {
                if (getId(resource) === iri) {
                    return resource;
                }
            });
        }
    };

    return {
        "q": quadsService,
        "t": triplesService,
        "r": resourceService
    };

});
