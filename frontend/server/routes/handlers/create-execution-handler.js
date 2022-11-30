"use strict";

const configuration = require("../../configuration");

const {readRequestBody, collectStreamToBuffer, guessFileName} =
  require("./request-parser");
const {HTTP, CONTENT} = require("./http-codes");
const {httpGetContentJsonLd, httpPostContent} = require("./http-request");

const BODY_OPTIONS = "options";

const BODY_CONFIGURATION = "configuration";

const BODY_CONTENT = "pipeline";

const BODY_INPUTS = "input";

const STORAGE_API_URL = configuration.storage.url + "/api/v1";

const MONITOR_API_URL = configuration.executor.monitor.url + "/api/v1";

async function handleCreateExecution(req, res) {
  const requestContent = await readRequestBody(req, BODY_OPTIONS);
  const options = secureOptions(requestContent);
  const pipeline = await securePipeline(req, requestContent);
  console.log(pipeline);
  if (pipeline === null) {
    res.status(HTTP.INVALID_REQUEST).json({
      "error": {
        "message": "Missing pipeline to execute."
      }
    });
    return;
  }
  // Unpack pipeline.
  const unpackedPipeline = await unpackPipeline(pipeline, options)
  const url = MONITOR_API_URL + "/executions";
  const parts = {
    "pipeline": [unpackedPipeline],
    "input": (requestContent[BODY_INPUTS] ?? []).map(item => ({
      "contentType": item["content-type"],
      "fileName": item["fileName"],
      "value": item["value"],
    })),
  };
  const headers = {
    "accept": req.headers["accept"] ?? CONTENT.JSONLD,
  };
  httpPostContent(url, parts, headers)
    .on("error", (error) => {
      console.error("Can't send data to execution-monitor.", error);
      res.status(HTTP.SERVER_ERROR).json({
        "error": {
          "message": "Can't send data to executor-monitor."
        }
      });
    })
    .pipe(res);
}

function secureOptions(requestContent) {
  // We use BODY_CONFIGURATION for backwards compatibility.
  let entry;
  if (requestContent[BODY_OPTIONS] !== undefined) {
    // TODO Add validation here as taking the first one may not work.
    entry = requestContent[BODY_OPTIONS][0];
  } else if (requestContent[BODY_CONFIGURATION] !== undefined) {
    // TODO Add validation here as taking the first one may not work.
    entry = requestContent[BODY_CONFIGURATION][0];
  } else {
    entry = createDefaultOptions();
  }
  return {
    "contentType": entry["contentType"],
    "fileName": entry["fileName"] ?? guessFileName("options", entry),
    "value": entry["value"],
  };
}

function createDefaultOptions() {
  return {
    "contentType": CONTENT.JSONLD,
    "fileName": "options.jsonld",
    "value": JSON.stringify({
      "@type": "http://etl.linkedpipes.com/ontology/ExecutionOptions",
      "http://linkedpipes.com/ontology/saveDebugData": true,
      "http://linkedpipes.com/ontology/deleteWorkingData": false
    }),
  };
}

/**
 * Content to import may be given as part of the request or
 */
async function securePipeline(req, requestContent) {
  // For backwards compatibility.
  const remote = req.query["iri"] ?? req.query["pipeline"];
  if (requestContent[BODY_CONTENT] !== undefined) {
    // TODO Add validation here as taking the first one may not work.
    const entry = requestContent[BODY_CONTENT][0];
    return {
      "contentType": entry["contentType"],
      "fileName": entry["fileName"] ?? guessFileName("pipeline", entry),
      "value": entry["value"],
    };
  } else if (remote !== undefined) {
    const content = await httpGetContentJsonLd(remote);
    return {
      "contentType": CONTENT.JSONLD,
      "fileName": "pipeline.jsonld",
      "value": content,
    };
  } else if (req.query["local-iri"] !== undefined) {
    const iri = encodeURIComponent(req.query["local-iri"]);
    // We use local access, this also help with firewall limitations.
    const url = STORAGE_API_URL + "/pipelines?iri=" + iri;
    const content = await httpGetContentJsonLd(url);
    return {
      "contentType": CONTENT.JSONLD,
      "fileName": "pipeline.jsonld",
      "value": content
    };
  }
  return null;
}

async function unpackPipeline(pipeline, options) {
  const url = STORAGE_API_URL + "/management/unpack";
  const parts = {
    "pipeline": [pipeline],
    "options": [options],
  };
  const headers = {
    "accept": CONTENT.JSONLD,
  };
  return new Promise((resolve, reject) => {
    httpPostContent(url, parts, headers)
      .on("response", async (response) => {
        resolve({
          "contentType": response["content-type"],
          "fileName": "pipeline.jsonld",
          "value": await collectStreamToBuffer(response),
        });
      })
      .on("error", reject);
  });
}

module.exports = {
  "handleCreateExecution": handleCreateExecution,
};
