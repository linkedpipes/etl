"use strict";

const request = require("request"); // https://github.com/request/request
const multiparty = require("multiparty");

const config = require("../configuration");
const errors = require("../error-codes");

const router = require("express").Router();
module.exports = router;

const storageApiUrl = config.storage.url + "/api/v1/pipelines";

router.get("", (req, res) => {
    const options = {
        "url": storageApiUrl + "/list",
        "headers": {
            "Accept": "application/ld+json"
        }
    };
    request.get(options)
        .on("error", (error) => handleConnectionError(res, error))
        .pipe(res);
});

function handleConnectionError(res, error) {
    console.error("Request failed:\n", error);
    res.status(503).json({
        "error": {
            "type": errors.CONNECTION,
            "source": "FRONTEND"
        }
    });
}

router.delete("/:id", (req, res) => {
    const url = storageApiUrl + "?iri="
        + encodeURI(config.storage.domain + req.originalUrl);
    request.del(url).pipe(res);
});

// TODO This is more part of the API.
router.get("/info", (req, res) => {
    const options = {
        "url": storageApiUrl + "/info",
        "headers": {
            "Accept": "application/ld+json"
        }
    };
    request.get(options)
        .on("error", (error) => handleConnectionError(res, error))
        .pipe(res);
});

router.get("/:id", function (req, res) {

    // Parse query IRI.
    let queryIri;
    let queryParams = "";
    let queryIndex = req.originalUrl.indexOf("?");
    if (queryIndex === -1) {
        queryIri = req.originalUrl;
    } else {
        queryIri = req.originalUrl.substring(0, queryIndex);
        queryParams = "&" + req.originalUrl.substring(queryIndex + 1);
    }

    const options = {
        "url": storageApiUrl + "?iri="
        + encodeURI(config.storage.domain + queryIri) + queryParams,
        "headers": {
            "Accept": "application/ld+json"
        }
    };

    request.get(options)
        .on("error", (error) => handleConnectionError(res, error))
        .pipe(res);
});

router.put("/:id", (req, res) => {
    const urlSuffix = getUrlSuffixWithoutParams(req);
    updatePipeline(res, urlSuffix, req);
});

/**
 * Return URL without parameters.
 */
function getUrlSuffixWithoutParams(req) {
    let urlSuffix = req.originalUrl;
    const argsIndex = urlSuffix.indexOf("?");
    return urlSuffix.substring(0, argsIndex !== -1 ? argsIndex : s.length);
}

function updatePipeline(res, urlSuffix, bodyStream) {
    const options = {
        "url": storageApiUrl + "?iri="
        + encodeURI(config.storage.domain + urlSuffix),
        "headers": {},
        "formData": {
            "pipeline": {
                "value": bodyStream,
                "options": {
                    "contentType": "application/ld+json; charset=utf-8",
                    "filename": "pipeline.jsonld"
                }
            }
        }
    };
    request.put(options)
        .on("error", (error) => handleConnectionError(res, error))
        .pipe(res);
}

router.post("", (req, res) => {
    const url = storageApiUrl + "";
    if (req.query["pipeline"]) {
        // We need to fetch pipeline from user given location.
        addPipelineAndPipe(req, res, url);
    } else {
        // Pipeline is provided, we can just pipe the content to the storage.
        req.pipe(request.post(url))
            .on("error", (error) => handleConnectionError(res, error))
            .pipe(res);
    }
});

// TODO This is more part of the API.
router.post("/localize", (req, res) => {
    const url = storageApiUrl + "/localize";
    if (req.query["pipeline"]) {
        // We need to fetch pipeline from user given location.
        addPipelineAndPipe(req, res, url);
    } else {
        // Pipeline is provided, we can just pipe the content to the storage.
        req.pipe(request.post(url)).pipe(res);
    }
});

/**
 * Parse user request for options.
 * Use query.pipeline parameter to extract
 */
function addPipelineAndPipe(req, res, url) {
    const form = new multiparty.Form();
    let importOptions = {
        "content": "",
        "type": ""
    };
    form.on("part", (part) => {
        // Read options from the user request.
        if (part.name === "options") {
            importOptions.type = part["headers"]["content-type"];
            part.on("data", (chunk) => importOptions.content += chunk);
        } else {
            part.resume();
        }
    });
    form.on("close", () => {
        if (importOptions.content === "") {
            importOptions = createDefaultImportOptions();
        }
        // Get pipeline.
        resolvePipeline(req, (error, http, pipeline) => {
            if (error) {
                handleConnectionError(res, error);
                return
            }
            postPipelineWithOptions(res, importOptions, pipeline, url);
        });
    });
    form.on("error", (error) => {
        console.error("Error when in form processing:\n", error);
        console.trace();
        res.status(500).json({"error": {
            "type": errors.INVALID_REQUEST,
            "source": "FRONTEND",
            "message": error
        }});
    });
    form.parse(req);
}

function createDefaultImportOptions() {
    return {
        "content": JSON.stringify({
            "@id": "http://localhost/options",
            "@type": "http://linkedpipes.com/ontology/UpdateOptions",
            "http://etl.linkedpipes.com/ontology/import": true
        }),
        "options": "appplication/ld+json"
    };
}

function resolvePipeline(req, callback) {
    let url;
    if (isTrue(req.query["fromLocal"])) {
        // Load directly from local storage, useful when public URL is
        // has restricted access.
        url = storageApiUrl + "?iri=" + encodeURI(req.query["pipeline"]);
    } else {
        url = req.query["pipeline"];
    }
    console.log("Resolve pipeline:", url);
    request.get({
        "url": url,
        "headers": {
            "Accept": "application/ld+json"
        }
    }, callback);
}

function isTrue(value) {
    if (value === undefined) {
        return false;
    }
    return value === "1" || value.toLowerCase() === "true";
}

function postPipelineWithOptions(res, importOptions, pipeline, url) {
    const options = {
        "url": url,
        "headers": {
            "Accept": "application/ld+json"
        },
        "formData": {
            "options": {
                "value": importOptions.content,
                "options": {
                    "contentType": importOptions.type,
                    "filename": "options.jsonld"
                }
            },
            "pipeline": {
                "value": pipeline,
                "options": {
                    "contentType": "application/ld+json",
                    "filename": "pipeline.jsonld"
                }
            }
        }
    };
    request.post(options)
        .on("error", (error) => handleConnectionError(res, error))
        .pipe(res);
}