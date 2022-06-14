"use strict";

const express = require("express");
const multiparty = require("multiparty");
const request = require("request"); // https://github.com/request/request

const config = require("../configuration");
const errors = require("../error-codes");
const executionFactory = require("../execution-factory");

const router = express.Router();
module.exports = router;

const pipelinesBaseUrl = config.storage.url + "/api/v1/pipelines";
const componentsBaseUrl = config.storage.url + "/api/v1/components";
const executionsApiUrl = config.executor.monitor.url + "/api/v1/executions";
const debugApiUrl = config.executor.monitor.url + "/api/v1/debug";

function updateAcceptHeader(headers) {
  // We use json-ld as default to make it easy to see pipelines from
  // browsers.
  if (headers.accept === undefined || headers.accept === "*/*") {
    return {
      ...headers,
      "accept": "application/ld+json"
    }
  }
  return headers;
}

function handleConnectionError(res, error, message = undefined) {
  console.error("Request failed:\n", error);
  res.status(503).json({
    "error": {
      "type": errors.CONNECTION,
      "error": error,
      "message": message
    }
  });
}

// Components

router.get("/components-list", (req, res) => {
  const options = {
    "url": componentsBaseUrl + "/list",
    "headers": updateAcceptHeader(req.headers),
  };
  request.get(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.get("/components/:type", (req, res) => {
  let url = componentsBaseUrl;
  switch (req.params.type) {
    case "interface":
      url += "/interface?";
      url += "iri=" + encodeURIComponent(req.query.iri);
      break;
    case "definition":
      url += "/definition?";
      url += "iri=" + encodeURIComponent(req.query.iri);
      break;
    case "effective":
      url += "/configEffective?";
      url += "iri=" + encodeURIComponent(req.query.iri);
      break;
    case "config":
      url += "/config?";
      url += "iri=" + encodeURIComponent(req.query.iri);
      break;
    case "configTemplate":
      url += "/configTemplate?";
      url += "iri=" + encodeURIComponent(req.query.iri);
      break;
    case "configDescription":
      url += "/configDescription?";
      url += "iri=" + encodeURIComponent(req.query.iri);
      break;
    case "static":
      url += "/static?";
      url += "iri=" + encodeURIComponent(req.query.iri);
      url += "&file=" + encodeURIComponent(req.query.file);
      break;
    case "dialog":
      url += "/dialog?";
      url += "iri=" + encodeURIComponent(req.query.iri);
      url += "&file=" + encodeURIComponent(req.query.file);
      url += "&name=" + encodeURIComponent(req.query.name);
      break;
    default:
      res.status(400).json({
        "error": {
          "type": errors.INVALID_REQUEST,
          "source": "FRONTEND",
          "message": "Unexpected type: " + req.params.type
        }
      });
      return;
  }
  request.get({"url": url, "headers": req.headers})
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.post("/components", (req, res) => {
  const url = componentsBaseUrl + "/";
  req.pipe(request.post(url, {"form": req.body}), {"end": false})
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.delete("/components", (req, res) => {
  const url = componentsBaseUrl + "?iri=" + encodeURIComponent(req.query.iri);
  request.del(url)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.post("/components/config", (req, res) => {
  const url = componentsBaseUrl + "/config?iri=" +
    encodeURIComponent(req.query.iri);
  req.pipe(request.post(url, {"form": req.body}), {"end": false})
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.post("/components/component", (req, res) => {
  const url = componentsBaseUrl + "/component?iri=" +
    encodeURIComponent(req.query.iri);
  req.pipe(request.post(url, {"form": req.body}), {"end": false})
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

// Pipeline

router.get("/pipelines-list", (req, res) => {
  const options = {
    "url": pipelinesBaseUrl + "/list",
    "headers": updateAcceptHeader(req.headers),
  };
  request.get(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.get("/pipelines", function (req, res) {
  const options = {
    "url": pipelinesBaseUrl,
    "qs": req.query,
    "headers": updateAcceptHeader(req.headers),
  };

  request.get(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.delete("/pipelines", (req, res) => {
  const url = pipelinesBaseUrl + "?iri=" + encodeURIComponent(req.query.iri);
  request.del(url).pipe(res);
});

router.put("/pipelines", (req, res) => {
  const options = {
    "url": pipelinesBaseUrl,
    "qs": req.query,
    "formData": {
      "pipeline": {
        "value": req,
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
});

router.post("/pipelines", (req, res) => {
  req.pipe(request.post(pipelinesBaseUrl))
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.post("/pipelines-copy", (req, res) => {
  readRequestContent(req)
    .then(async content => {
      const options = content["options"] ?? createDefaultImportOptions();
      const pipeline = await fetchPipeline(req);
      const parts = createPipelinePostParts(options, pipeline);
      const url = pipelinesBaseUrl;
      postMultipart(url, parts)
        .on("error", (error) => handleCantPostToStorage(res, error))
        .pipe(res);
    })
    .catch(error => handleCantFetchPipeline(res, error));
});

function readRequestContent(req) {
  const form = new multiparty.Form();
  const result = {};
  return new Promise((resolve, reject) => {
    form.on("part", (part) => {
      const object = {
        "contentType": part["headers"]["content-type"],
        "content": "",
      };
      result[part.name] = object;
      part.on("data", (chunk) => object.content += chunk);
    });
    form.on("close", () => {
      resolve(result);
    });
    form.on("error", reject);
    form.parse(req);
  });
}

function createDefaultImportOptions() {
  return {
    "contentType": "application/ld+json",
    "content": JSON.stringify({
      "@id": "http://localhost/options",
      "@type": "http://linkedpipes.com/ontology/UpdateOptions",
      "http://etl.linkedpipes.com/ontology/import": true
    }),
  };
}

function fetchPipeline(req) {
  const options = {
    "headers": {
      "accept": "application/ld+json"
    }
  };
  if (isTrue(req.query["fromLocal"])) {
    // Load directly from the storage.
    // Useful when public URL is has restricted access.
    options["url"] = pipelinesBaseUrl + "?iri=" + encodeURIComponent(req.query["iri"]);
  } else {
    options["url"] = req.query["iri"];
  }
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

function isTrue(value) {
  return value === "1" || value?.toLowerCase() === "true";
}

function createPipelinePostParts(options, pipeline) {
  return {
    "options": {
      "value": options["content"],
      "contentType": options["contentType"],
      "filename": "options",
    },
    "pipeline": {
      "value": pipeline,
      "contentType": "application/ld+json",
      "filename": "pipeline",
    },
  };
}

function postMultipart(url, parts) {
  const formData = {};
  for (const [name, value] of Object.entries(parts)) {
    formData[name] = {
      "value": value["value"],
      "options": {
        "contentType": value["contentType"],
        "filename": value["filename"]
      }
    }
  }
  const options = {
    "url": url,
    "headers": {
      "Accept": "application/ld+json"
    },
    "formData": formData
  };
  return request.post(options);
}

function handleCantPostToStorage(res, error) {
  console.error("Can't POST data to storage.", error);
  res.status(503).json({
    "error": {
      "type": errors.CONNECTION,
      "error": error,
      "message": "Can't POST data to storage."
    }
  });
}

function handleCantFetchPipeline(res, error) {
  console.error("Can't copy pipeline from remote.", error);
  res.status(503).json({
    "error": {
      "type": errors.CONNECTION,
      "error": error,
      "message": "Can't copy pipeline from remote."
    }
  });
}

router.post("/pipelines-localize", (req, res) => {
  readRequestContent(req)
    .then(async content => {
      const options = content["options"] ?? createDefaultImportOptions();
      const pipeline = await fetchPipeline(req);
      const parts = createPipelinePostParts(options, pipeline);
      const url = pipelinesBaseUrl + "/localize";
      postMultipart(url, parts)
        .on("error", (error) => handleCantPostToStorage(res, error))
        .pipe(res);
    })
    .catch(error => handleCantFetchPipeline(res, error));
});

// Execution

router.get("/executions-list", (req, res) => {
  let uri = executionsApiUrl;
  if (req.query["changedSince"]) {
    uri += "?changedSince=" + req.query["changedSince"];
  }
  const options = {
    "url": uri,
    "headers": updateAcceptHeader(req.headers),
  };
  request.get(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.get("/executions", (req, res) => {
  const options = {
    "url": executionsApiUrl + "/",
    "headers": updateAcceptHeader(req.headers),
    "qs": req.query,
  };
  request.get(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.post("/executions", (req, res) => {
  executionFactory.create(req, res);
});

router.post("/executions-cancel", (req, res) => {
  const options = {
    "url": executionsApiUrl + "/cancel",
    "headers": updateAcceptHeader(req.headers),
    "qs": req.query,
  };
  req.pipe(request.post(options))
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.delete("/executions", (req, res) => {
  const options = {
    "url": executionsApiUrl + "/",
    "headers": updateAcceptHeader(req.headers),
    "qs": req.query,
  };
  request.del(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.get("/executions-pipeline", (req, res) => {
  const options = {
    "url": executionsApiUrl + "/pipeline",
    "headers": updateAcceptHeader(req.headers),
    "qs": req.query,
  };
  request.del(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.get("/executions-overview", (req, res) => {
  const options = {
    "url": executionsApiUrl + "/overview",
    "headers": updateAcceptHeader(req.headers),
    "qs": req.query,
  };
  request.del(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.get("/executions-logs", (req, res) => {
  const options = {
    "url": executionsApiUrl + "/logs",
    "headers": updateAcceptHeader(req.headers),
    "qs": req.query,
  };
  request.del(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.get("/executions-logs-tail", (req, res) => {
  const options = {
    "url": executionsApiUrl + "/logs-tail",
    "headers": updateAcceptHeader(req.headers),
    "qs": req.query,
  };
  request.del(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.get("/executions-messages", (req, res) => {
  const options = {
    "url": executionsApiUrl + "/messages",
    "headers": updateAcceptHeader(req.headers),
    "qs": req.query, // execution, component
  };
  request.del(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

// Debug

router.get("/debug/metadata/**", (req, res) => {
  let url = debugApiUrl + req.originalUrl.replace("/api/v1/debug", "");
  request.get(url)
    .on("error", (error) => handleConnectionError(res, error))
    .on("response", remoteRes => {
      remoteRes.headers["Access-Control-Allow-Origin"] = "*";
    })
    .pipe(res);
});

router.get("/debug/data/**", (req, res) => {
  let url = debugApiUrl + req.originalUrl.replace("/api/v1/debug", "");
  request.get(url)
    .on("error", (error) => handleConnectionError(res, error))
    .on("response", remoteRes => {
      remoteRes.headers["Access-Control-Allow-Origin"] = "*";
    })
    .pipe(res);
});

// Other

router.get("/status", (req, res) => {
  res.status(200).json({
    "instance": {
      "label": config.instanceLabel
    }
  });
});

router.get("/usage", (req, res) => {
  const options = {
    "url": config.storage.url + "/api/v1/components/usage?iri=" +
      encodeURIComponent(req.query.iri),
    "headers": req.headers
  };
  request.get(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.get("/jars/file", (req, res) => {
  let iri = config.storage.url + "/api/v1/jars/file?";
  iri += "iri=" + encodeURIComponent(req.query.iri);
  //
  request.get(iri)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.get("/export", (req, res) => {
  const options = {
    "url": config.storage.url + "/api/v1/export",
    "qs": req.query,
    "headers": req.headers
  };
  request.get(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.get("/designer", (req, res) => {
  const options = {
    "url": pipelinesBaseUrl + "/info",
    "headers": updateAcceptHeader(req.headers),
  };
  request.get(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});



