"use strict";

const configuration = require("../../configuration");

const {readRequestBody, guessFileName} = require("./request-parser");
const {HTTP, CONTENT} = require("./http-codes");
const {httpGetContentJsonLd, httpPostContent} = require("./http-request");

const BODY_OPTIONS = "options";

const STORAGE_API_URL = configuration.storage.url + "/api/v1";

async function handlePipelineCopy(req, res) {
  const requestContent = await readRequestBody(req);
  const options = secureOptions(requestContent);
  const content = await securePipeline(req);
  if (content === null) {
    res.status(HTTP.INVALID_REQUEST).json({
      "error": {
        "message": "Provide local pipeline URL using query 'local-iri' argument."
      },
    });
  }
  const url = STORAGE_API_URL + "/management/import";
  const parts = {
    "options": [options],
    "content": [content],
  };
  const headers = {
    "accept": req.headers["accept"] ?? "application/ld+json",
  };
  httpPostContent(url, parts, headers)
    .on("error", (error) => {
      console.error("Can't send data to storage.", error);
      res.status(HTTP.SERVER_ERROR).json({
        "error": {
          "message": "Can't send data to storage."
        }
      });
    })
    .pipe(res);
}

function secureOptions(requestContent) {
  if (requestContent[BODY_OPTIONS] === undefined) {
    return createDefaultOptions();
  }
  // TODO Add validation here as taking the first one may not work.
  const entry = requestContent[BODY_OPTIONS][0];
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
      "@id": "http://localhost/options",
      "@type": "http://linkedpipes.com/ontology/UpdateOptions",
      "http://etl.linkedpipes.com/ontology/updateExistingTemplates": false,
      "http://etl.linkedpipes.com/ontology/importNewTemplates": false,
      "http://etl.linkedpipes.com/ontology/importPipeline": true,
    }),
  };
}

/**
 * Content to import may be given as part of the request or
 */
async function securePipeline(req) {
  if (req.query["local-iri"] !== undefined) {
    const iri = encodeURIComponent(req.query["local-iri"]);
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

module.exports = {
  "handlePipelineCopy": handlePipelineCopy,
};
