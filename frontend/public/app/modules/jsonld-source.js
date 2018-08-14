/**
 * JsonLD source for a repository.
 */
((definition) => {
    if (typeof define === "function" && define.amd) {
        define(["./jsonld-to-json", "./http"], definition);
    } else if (typeof module !== "undefined") {
        const jsonLdToJson = require("./jsonld-to-json");
        const http = require("./http");
        module.exports = definition(jsonLdToJson, http);
    }
})((jsonLdToJson, http) => {
    "use strict";

    const TOMBSTONE_TEMPLATE = {
        "id": {
            "$resource": null
        }
    };

    const METADATA_TEMPLATE = {
        "timestamp": {
            "$property": "http://etl.linkedpipes.com/ontology/serverTime",
            "$type": "plain-string"
        }
    };

    const METADATA_TYPE = "http://etl.linkedpipes.com/ontology/Metadata";

    function createJsonLdSource(config) {
        if (config.incrementalUpdateSupport) {
            return createWithIncrementalUpdateSupport(config);
        } else {
            return create(config);
        }
    }

    function createWithIncrementalUpdateSupport(config) {
        return {
            "fetch": (changedSince) => fetchChangedSince(changedSince, config),
            "deleteById": deleteById,
            "incrementalUpdateSupport": true
        };
    }

    function fetchChangedSince(changedSince, config) {
        let url = config.url;
        if (changedSince !== undefined) {
            url += "?changedSince=" + changedSince;
        }
        return fetchItems(
            url,
            config.itemType,
            config.tombstoneType,
            config.itemTemplate);
    }

    function fetchItems(url, itemType, tombstoneType, itemTemplate) {
        return http.getJsonLd(url).then((response) => {
            const payload = response.json;
            return {
                "data": jsonLdToJson(payload, itemType, itemTemplate),
                "tombstones": getTombstonesIds(payload, tombstoneType),
                "timeStamp": getTimeStamp(payload)
            }
        })
    }

    function getTimeStamp(payload) {
        const metadata = jsonLdToJson(
            payload, METADATA_TYPE, METADATA_TEMPLATE);
        if (metadata.length === 0) {
            return undefined;
        } else {
            return metadata[0].timestamp;
        }
    }

    function getTombstonesIds(payload, type) {
        const tombstones = jsonLdToJson(payload, type, TOMBSTONE_TEMPLATE);
        return tombstones.map(item => item.id);
    }

    function create(config) {
        return {
            "fetch": () => fetch(config),
            "deleteById": deleteById,
            "incrementalUpdateSupport": false
        };
    }

    function fetch(config) {
        return fetchItems(
            config.url,
            config.itemType,
            config.tombstoneType,
            config.itemTemplate);
    }

    function deleteById(id) {
        const url = id;
        return http.delete(url);
    }

    const createBuilder = () => {
        const config = {
            "incrementalUpdateSupport": false
        };
        const builder = {};
        builder["url"] =
            (url) => config["url"] = url;
        builder["itemType"] =
            (type) => config["itemType"] = type;
        builder["tombstoneType"] =
            (type) => config["tombstoneType"] = type;
        builder["itemTemplate"] =
            (template) => config["itemTemplate"] = template;
        builder["supportIncrementalUpdate"] =
            () => config["incrementalUpdateSupport"] = true;
        builder["build"] = () => createJsonLdSource(config);
        return builder;
    };

    return {
        "create": createJsonLdSource,
        "createBuilder": createBuilder
    }

});
