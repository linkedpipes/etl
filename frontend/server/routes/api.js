"use strict";

const express = require("express");
const request = require("request"); // https://github.com/request/request

const config = require("../configuration");
const errors = require("../error-codes");
const executionFactory = require("../execution-factory");

const {handleImport} = require("./api/import-handler");
const {handleLocalize} = require("./api/localize-handler");
const {handlePipelineCopy} = require("./api/pipeline-copy-handler");

const router = express.Router();
module.exports = router;

const pipelinesBaseUrl = config.storage.url + "/api/v1/pipelines";
const componentsBaseUrl = config.storage.url + "/api/v1/components";
const executionsApiUrl = config.executor.monitor.url + "/api/v1/executions";
const debugApiUrl = config.executor.monitor.url + "/api/v1/debug";

const HTTP_SERVER_ERROR = 500;

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
      url += "/effective-configuration?";
      url += "iri=" + encodeURIComponent(req.query.iri);
      break;
    case "config":
      url += "/configuration?";
      url += "iri=" + encodeURIComponent(req.query.iri);
      break;
    case "configTemplate":
      url += "/configuration-template?";
      url += "iri=" + encodeURIComponent(req.query.iri);
      break;
    case "configDescription":
      url += "/configuration-description?";
      url += "iri=" + encodeURIComponent(req.query.iri);
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
  const url = componentsBaseUrl + "/configuration?iri=" +
    encodeURIComponent(req.query.iri);
  req.pipe(request.put(url, {"form": req.body}), {"end": false})
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.post("/components/component", (req, res) => {
  const url = componentsBaseUrl + "/component?iri=" +
    encodeURIComponent(req.query.iri);
  req.pipe(request.put(url, {"form": req.body}), {"end": false})
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
  handlePipelineCopy(req, res).catch(error => {
    console.error("Can't copy pipeline.", error);
    res.status(HTTP_SERVER_ERROR).json({
      "error": {
        "message": "Can't execute request."
      }
    })
  });
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
  request.get(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.get("/executions-overview", (req, res) => {
  const options = {
    "url": executionsApiUrl + "/overview",
    "headers": updateAcceptHeader(req.headers),
    "qs": req.query,
  };
  request.get(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.get("/executions-logs", (req, res) => {
  const options = {
    "url": executionsApiUrl + "/logs",
    "headers": updateAcceptHeader(req.headers),
    "qs": req.query,
  };
  request.get(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.get("/executions-logs-tail", (req, res) => {
  const options = {
    "url": executionsApiUrl + "/logs-tail",
    "headers": updateAcceptHeader(req.headers),
    "qs": req.query,
  };
  request.get(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.get("/executions-messages", (req, res) => {
  const options = {
    "url": executionsApiUrl + "/messages",
    "headers": updateAcceptHeader(req.headers),
    "qs": req.query, // execution, component
  };
  request.get(options)
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

router.get("/export", (req, res) => {
  const options = {
    "url": config.storage.url + "/api/v1/management/export",
    "qs": req.query,
    "headers": req.headers
  };
  request.get(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.get("/designer", (req, res) => {
  const options = {
    "url": config.storage.url + "/api/v1/management/assistant",
    "headers": updateAcceptHeader(req.headers),
  };
  request.get(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});

router.post("/import", (req, res) => {
  handleImport(req, res).catch(error => {
    console.error("Import handler failed.", error);
    res.status(HTTP_SERVER_ERROR).json({
      "error": {
        "message": "Can't execute request."
      }
    })
  });
});

router.post("/localize", (req, res) => {
  handleLocalize(req, res).catch(error => {
    console.error("Localize handler failed.", error);
    res.status(HTTP_SERVER_ERROR).json({
      "error": {
        "message": "Can't execute request."
      }
    })
  });
});
