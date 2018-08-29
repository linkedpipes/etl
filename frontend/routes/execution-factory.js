"use strict";

const request = require("request"); // https://github.com/request/request
const httpRequest = require("http").request;
const parseUrl = require("url").parse;
const multiparty = require("multiparty");
const PassThrough = require("stream").PassThrough;

const config = require("../modules/configuration");
const errors = require("./error-codes");

module.exports = {
    "create": handleCreateRequest
};

const monitorApiUrl = config.executor.monitor.url + "executions";

function handleCreateRequest(req, res) {
    const contentType = req.headers["content-type"];
    if (contentType === undefined ||
        contentType.toLowerCase().indexOf("multipart/form-data") === -1) {
        createFromNonMultipartRequest(req, res);
    } else {
        createFromMultipartRequest(req, res);
    }
}

function createFromNonMultipartRequest(req, res) {
    if (req.query["pipeline"] === undefined) {
        res.status(400).json({
            "error": {
                "type": errors.INVALID_REQUEST,
                "message": "Missing pipeline"
            }
        });
        return;
    }
    // We have the pipeline IRI in the URL. The content of the body is
    // considered to be a configuration.
    const pipeline = {
        "iri": req.query["pipeline"]
    };
    let configuration = "";
    req.on("data", (chunk) => configuration += chunk);
    req.on("end", () => {
        unpackPipeline(pipeline, configuration, (error, result) => {
            if (error) {
                console.error(
                    "Can't unpack pipeline from url:", pipeline.iri, error);
                res.status(500).json({"error": {"type": errors.ERROR}});
                return;
            }
            // Post execution.
            const formData = {
                "format": "application/ld+json",
                "pipeline": {
                    "value": result,
                    "options": {
                        "contentType": "application/octet-stream",
                        "filename": "options.jsonld"
                    }
                }
            };
            console.time("[POST] /executions");
            const options = {
                "url": monitorApiUrl,
                "formData": formData,
                "headers": {
                    "Accept": "application/json"
                }
            };
            request.post(options)
                .on("error", (error) => handleConnectionError(res, error))
                .on("response", () => console.timeEnd("[POST] /executions"))
                .pipe(res);
        });
    });

}

function handleConnectionError(res, error) {
    console.error("Request failed:\n", error);
    console.trace();
    res.status(503).json({"error": {"type": errors.CONNECTION}});
}

function unpackPipeline(pipeline, optionsAsString, callback) {
    if (optionsAsString === undefined || optionsAsString === "") {
        optionsAsString = "{}";
    }
    const formData = {
        "options": {
            "value": optionsAsString,
            "options": {
                "contentType": "application/ld+json",
                "filename": "options.jsonld"
            }
        }
    };
    const headers = {
        "Accept": "application/ld+json"
    };
    let url = config.storage.url + "/api/v1/pipelines/unpack";
    if (pipeline.iri) {
        url += "?iri=" + encodeURIComponent(pipeline.iri);
    }
    if (pipeline.body) {
        formData["pipeline"] = {
            "value": pipeline.body,
            "options": {
                "contentType": "application/ld+json",
                "filename": "pipeline.jsonld"
            }
        }
    }
    //
    console.time("[POST] /unpack");
    request.post({"url": url, "formData": formData, "headers": headers},
        (error, httpResponse, body) => {
            console.timeEnd("[POST] /unpack");
            if (httpResponse.statusCode === 404) {
                callback({"userError": {"type": errors.MISSING}}, body);
            } else if (httpResponse.statusCode !== 200) {
                callback({"userError": {"type": errors.ERROR}}, body);
            } else {
                callback(error, body);
            }
        });
}

function createFromMultipartRequest(req, res) {
    console.log("Creating execution from multipart request");
    // We will stream resources to the executor-monitor, once we find
    // a pipeline we will unpack it and then pipe it.
    const postUrl = parseUrl(monitorApiUrl);
    const boundaryString = "------------------------a76d7ee9b9b2b7ef";
    const postOptions = {
        "host": postUrl.postname,
        "port": postUrl.port,
        "path": postUrl.path,
        "method": "POST",
        "headers": {
            "Content-Type": "multipart/form-data; boundary=" + boundaryString,
            "Transfer-Encoding": "chunked",
            "Accept": "application/json",
            "Connection": "keep-alive"
        }
    };
    let pipePostRequestResponse = true;
    const postRequest = httpRequest(postOptions, (executorMonitorRes) => {
        // Pipe result back to caller if pipePost is true.
        if (pipePostRequestResponse) {
            executorMonitorRes.pipe(res);
        }
    });
    const postStream = new StreamCombiner(postRequest);
    const form = new multiparty.Form();
    let isFirstBoundary = true;
    let configurationAsString = ""; // Default is empty configuration.
    let pipelineAsString = "";
    const shouldUnpackPipeline = !req.query["unpacked_pipeline"];

    form.on("part", (part) => {
        if (part.name === "configuration") {
            part.on("data", (chunk) => configurationAsString += chunk);
            return;
        }
        if (part.name === "pipeline" && shouldUnpackPipeline) {
            part.on("data", chunk => pipelineAsString += chunk);
            return;
        }
        // Pipe all the rest to executor-monitor server.
        const textStream = new PassThrough();
        if (isFirstBoundary) {
            isFirstBoundary = false;
        } else {
            textStream.write("\r\n");
        }
        createFormInfoEntry(textStream, boundaryString, part);
        textStream.end();
        postStream.append(textStream);
        postStream.append(part);
    });

    form.on("close", () => {
        const pipeline = {};
        if (req.query["pipeline"] !== undefined) {
            pipeline.iri = req.query["pipeline"];
        } else if (shouldUnpackPipeline) {
            // Pipeline (frontend format) form the POST request.
            pipeline.body = pipelineAsString;
        } else {
            // Unpacked pipeline was send as a part of request,
            // so we just need to close the request.
            const textStream = new PassThrough();
            createCloseEntry(textStream, boundaryString);
            textStream.end();
            postStream.append(textStream);
            postStream.end();
            return;
        }
        unpackPipeline(pipeline, configurationAsString, (error, result) => {
            if (error) {
                console.error("Can't unpack pipeline.", error, result);
                if (error.userError) {
                    res.status(500).json({"error": error.userError});
                } else {
                    res.status(500).json({"error": {"type": errors.ERROR}});
                }
                pipePostRequestResponse = false;
                postStream.end();
                return;
            }
            // Pipe pipeline into the stream.
            const textStream = new PassThrough();
            if (!isFirstBoundary) {
                textStream.write("\r\n");
            }
            createPipelineEntry(textStream, boundaryString, result);
            textStream.end();
            postStream.append(textStream);
            postStream.end();
            console.log("createFromMultipartRequest ... done")
        });
    });

    form.on("error", (error) => {
        console.error("Can't create pipeline from multipart request.", error);
        res.status(500).json({"error": {"type": errors.ERROR}});
        pipePostRequestResponse = false;
        postStream.end();
    });

    form.parse(req);
}

function createFormInfoEntry(textStream, boundaryString, part) {
    textStream.write("--" + boundaryString + "\r\n");
    textStream.write("Content-Disposition:" +
        part.headers["content-disposition"] + "\r\n");
    textStream.write("Content-Type: " +
        part.headers["content-type"] + "\r\n\r\n");
}

function createCloseEntry(textStream, boundaryString) {
    textStream.write("\r\n--" + boundaryString + "--\r\n");
}

function createPipelineEntry(textStream, boundaryString, pipeline) {
    textStream.write("--" + boundaryString + "\r\n");
    textStream.write(
        "Content-Disposition: form-data; name=\"pipeline\"; " +
        "filename=\"pipeline.jsonld\"\r\n");
    textStream.write("Content-Type: application/octet-stream\r\n\r\n");
    textStream.write(pipeline);
    createCloseEntry(textStream, boundaryString)
}

/**
 * Append multiple streams into one.
 * Implementation rely on a single thread execution (normal in NodeJS).
 */
function StreamCombiner(stream) {

    // Wrapped stream.
    this.target = stream;

    // Input streams to combine.
    this.sources = [];

    // Index of currently streaming stream.
    this.index = -1;

    // If true then close once there are no streams.
    this.closeOnEnd = false;

    this.streaming = false;

    // If true stream on this.index ended and was closed.
    this.currentClosed = false;

    const _this = this;

    // Append given stream to the wrapped stream.
    this.append = function (stream) {
        _this.sources.push(stream);
        if (!_this.streaming) {
            pipeNext();
        }
    };

    this.end = function () {
        // Close after the last stream is processed.
        _this.closeOnEnd = true;
        if (!_this.streaming) {
            pipeNext();
        }
    };

    // Look for next stream and pipe it.
    function pipeNext() {
        _this.streaming = true;
        closeCurrent();
        if (_this.index + 1 < _this.sources.length) {
            // Send new stream.
            _this.index += 1;
            const stream = _this.sources[_this.index];
            stream.on("end", pipeNext);
            stream.pipe(_this.target, {"end": false});
            _this.currentClosed = false;
        } else {
            // Nothing more to stream.
            if (_this.closeOnEnd) {
                _this.target.end();
            } else {
            }
            // Wait for next stream.
            _this.streaming = false;
        }
    }

    function closeCurrent() {
        if (_this.index > -1 && !_this.currentClosed) {
            // Close stream under this.index.
            const oldStream = _this.sources[_this.index];
            oldStream.unpipe(_this.target);
            _this.currentClosed = true;
        }
    }

}



