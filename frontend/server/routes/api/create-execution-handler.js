"use strict";

const multiparty = require("multiparty");
const request = require("request");
const configuration = require("../../configuration");

const BODY_OPTIONS = "options";

const BODY_CONTENT = "pipeline";

const BODY_INPUTS = "input";

const HTTP_INVALID_REQUEST = 400;

const HTTP_SERVER_ERROR = 500;

const STORAGE_API_URL = configuration.storage.url + "/api/v1";

const MONITOR_API_URL = configuration.executor.monitor.url + "/api/v1";

async function handleCreateExecution(req, res) {
  const requestContent = await readRequestBody(req, BODY_OPTIONS);
  const options = secureOptions(requestContent);
  const pipeline = await securePipeline(req, requestContent);
  if (pipeline === null) {
    res.status(HTTP_INVALID_REQUEST).json({
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
      "fileName": fileNameFromContentDisposition(
        item["headers"]["content-disposition"]),
      "value": item["value"],
    })),
  };
  const headers = {
    "accept": req.headers["accept"] ?? "application/ld+json",
  };
  postMultipart(url, parts, headers)
    .on("error", (error) => {
      console.error("Can't send data to storage.", error);
      res.status(HTTP_SERVER_ERROR).json({
        "error": {
          "message": "Can't send data to executor-monitor."
        }
      });
    })
    .pipe(res);
}

async function readRequestBody(req, bodyName) {
  if (req.is("multipart/form-data")) {
    return readMultipartRequestBody(req);
  } else {
    return readRawRequestBody(req, bodyName);
  }
}

async function readMultipartRequestBody(req) {
  const form = new multiparty.Form();
  const result = {};
  return new Promise((resolve, reject) => {
    form.on("part", (part) => {
      const object = {
        "headers": part["headers"],
        "value": Buffer.alloc(0),
      };
      // There can be multiple parts with same name.
      result[part.name] = [...result[part.name] ?? [], object];
      //
      collectStreamToBuffer(part)
        .then(content => object.value = content)
        .catch(reject);
    });
    form.on("close", () => resolve(result));
    form.on("error", reject);
    form.parse(req);
  });
}

async function collectStreamToBuffer(source) {
  return new Promise((resolve, reject) => {
    const collector = [];
    source.on("data", (chunk) => collector.push(chunk));
    source.on("end", () => resolve(Buffer.concat(collector)));
    source.on("error", reject);
  });
}

async function readRawRequestBody(req, bodyName) {
  return {
    [bodyName]: [{
      "headers": {
        "content-type": req.get("content-type"),
      },
      "value": await collectStreamToBuffer(req),
    }],
  };
}

function secureOptions(requestContent) {
  if (requestContent[BODY_OPTIONS] === undefined) {
    return createDefaultOptions();
  }
  // TODO Add validation here as taking the first one may not work.
  const data = requestContent[BODY_OPTIONS][0];
  const headers = data["headers"];
  const fileName =
    fileNameFromContentDisposition(headers["content-disposition"]) ??
    fileNameFromContentType("options", headers["content-type"]);
  return {
    "contentType": headers["content-type"],
    "fileName": fileName,
    "value": data["value"],
  };
}

function fileNameFromContentDisposition(contentDisposition) {
  if (contentDisposition === undefined) {
    return undefined;
  }
  const groups = contentDisposition.match('filename="([^"]+)"');
  if (groups.length === 2) {
    return groups[1];
  } else {
    return undefined;
  }
}

function fileNameFromContentType(name, contentType) {
  // TODO Use content to detect the extension.
  return name + ".jsonld";
}

function createDefaultOptions() {
  return {
    "contentType": "application/ld+json",
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
  if (requestContent[BODY_CONTENT] !== undefined) {
    // TODO Add validation here as taking the first one may not work.
    const data = requestContent[BODY_CONTENT][0];
    return {
      "contentType": data["headers"]["content-type"],
      "fileName": fileNameFromContentDisposition(data),
      "value": data["content"],
    };
  } else if (req.query["iri"] !== undefined) {
    const url = req.query["iri"];
    const content = await fetchUrlAsJsonLd(url);
    return {
      "contentType": "application/ld+json",
      "fileName": "content.jsonld",
      "value": content,
    };
  } else if (req.query["local-iri"] !== undefined) {
    const iri = encodeURIComponent(req.query["local-iri"]);
    const url = STORAGE_API_URL + "/pipelines?iri=" + iri;
    const content = await fetchUrlAsJsonLd(url);
    return {
      "contentType": "application/ld+json",
      "fileName": "content.jsonld",
      "value": content
    };
  }
  return null;
}

async function fetchUrlAsJsonLd(url) {
  const options = {
    "headers": {
      "accept": "application/ld+json",
    },
    "url": url,
  };
  return new Promise((resolve, reject) => {
    request.get(options, (error, http, body) => {
      if (error) {
        reject(error);
        return;
      }
      resolve(body);
    }).on("error", reject);
  });
}

async function unpackPipeline(pipeline, options) {
  const url = STORAGE_API_URL + "/management/unpack";
  const parts = {
    "pipeline": [pipeline],
    "options": [options],
  };
  const headers = {
    "Accept": "application/ld+json",
  };
  return new Promise((resolve, reject) => {
    postMultipart(url, parts, headers)
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

/**
 * Each part must contain: value, contentType and fileName.
 */
function postMultipart(url, parts, headers) {
  console.log("------- POST:", url, "\n", parts);
  const formData = {};
  for (const [name, items] of Object.entries(parts)) {
    if (items === null || items === undefined || items.length === 0) {
      continue;
    }
    formData[name] = items.map(item => ({
      "value": item["value"],
      "options": {
        "contentType": item["contentType"],
        "filename": item["fileName"],
      },
    }));
  }
  const options = {
    "url": url,
    "headers": headers,
    "formData": formData
  };
  return request.post(options);
}

module.exports = {
  "handleCreateExecution": handleCreateExecution,
};
