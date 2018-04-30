/**
 * Provides template based conversion from JSON-LD to JSON.
 */
((definition) => {
    if (typeof define === "function" && define.amd) {
        define(["jsonld"], definition);
    } else if (typeof module !== "undefined" && require) {
        const jsonld = require("./jsonld");
        module.exports = definition(jsonld);
    }
})((jsonld) => {
    "use strict";

    function convert(data, dataType, template) {
        const output = [];
        jsonld.q.iterateResources(data, (resource, graph) => {
            if (!isOfGivenType(resource, dataType)) {
                return;
            }
            output.push(evaluateTemplate(data, graph, resource, template));
        });
        return output;
    }

    function isOfGivenType(resource, dataType) {
        // TODO Replace with use of JsonLD.
        const type = resource["@type"];
        if (Array.isArray(type)) {
            return type.indexOf(dataType) !== -1;
        } else {
            return type === dataType;
        }
    }

    function evaluateTemplate(data, graph, resource, template) {
        const result = {};
        for (let key in template) {
            var item = template[key];
            // Check for $resource template.
            if (item["$resource"] !== undefined) {
                result[key] = resource["@id"];
                continue;
            }
            // Check for object without $property - ie. has sub-objects.
            const property = item["$property"];
            if (property === undefined) {
                result[key] = evaluateTemplate(data, graph, resource, item);
                continue;
            }
            // Check for the value.
            const value = resource[property];
            if (value === undefined) {
                // TODO Add array as a type and sub-type and handle this better.
                if (item["$type"] === "plain-array") {
                    result[key] = [];
                }
                continue;
            }
            if (item["$oneToMany"] !== undefined) {
                result[key] = convertOneToMany(value, data, graph, item);
            } else if (item["$oneToOne"] !== undefined) {
                convertOneToOne(value, result, data, graph, item);
            } else {
                result[key] = convertByType(resource, property, item["$type"]);
            }
        }
        return result;
    }

    function convertByType(resource, property, type) {
        if (type === "plain-string") {
            return jsonld.r.getPlainString(resource, property);
        } else if (type === "date") {
                return jsonld.r.getDate(resource, property);
        } else if (type === "iri") {
            return jsonld.r.getIRI(resource, property);
        } else if (type === "plain-array") {
            const values = jsonld.r.getValue(resource, property);
            if (values === undefined) {
                return [];
            }
            // TODO Move to JsonLD.
            const plainValues = [];
            values.forEach(item => {
                if (item["@value"] === undefined) {
                    plainValues.push(item);
                } else {
                    plainValues.push(item["@value"]);
                }
            });
            return plainValues;
        } else {
            console.error("Unknown template type", type, "for", property,
                new Error());
            return undefined;
        }
    }

    function convertOneToMany(value, data, graph, item) {
        const output = [];
        if (Array.isArray(value)) {
            value.forEach((item) => {
                const iri = jsonld.r.getId(item);
                const resource = jsonld.q.getResource(data, graph, iri);
                if (resource == undefined) {
                    console.warn("Unresolved reference:", iri);
                    return;
                }
                const newObject = evaluateTemplate(
                    data, graph, resource, item["$oneToMany"]);
                output.push(newObject);
            });
        } else {
            // We can use convertOneToOne as there is only one object.
            output.push(convertOneToOne(value, {}, data, graph, item));
        }
        return output;
    }

    function convertOneToOne(value, result, data, graph, templateItem) {
        let resource = loadResource(value, data, graph);
        if (resource === undefined) {
            return;
        }
        const toAdd = evaluateTemplate(
            data, graph, resource, templateItem['$oneToOne']);
        mergeRightToLeft(result, toAdd);
    }

    function loadResource(value, data, graph) {
        if (Array.isArray(value)) {
            if (value[0] && value[0]['@id']) {
                return jsonld.q.getResource(data, graph, value[0]['@id']);
            }
        } else {
            return jsonld.q.getResource(data, graph, value['@id']);
        }
    }

    function mergeRightToLeft(left, right) {
        for (let key in right) {
            left[key] = right[key];
        }
    }

    return convert;

});