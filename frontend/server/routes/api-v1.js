"use strict";

const express = require("express");

const configuration = require("../configuration");

const {httpGetForProxy, httpPostForProxy, httpDeleteForProxy,
  httpPutForProxy} = require("./handlers/http-request");
const {HTTP} = require("./handlers/http-codes");

const {handleImport} = require("./handlers/import-handler");
const {handleLocalize} = require("./handlers/localize-handler");
const {handlePipelineCopy} = require("./handlers/pipeline-copy-handler");
const {handleCreateExecution} = require("./handlers/create-execution-handler");
const {handleUpdatePipeline} = require("./handlers/update-pipeline-handler");

const router = express.Router();
module.exports = router;

const STORAGE_API_URL = configuration.storage.url + "/api/v1";

const MONITOR_API_URL = configuration.executor.monitor.url + "/api/v1";

// Components

router.get("/components-list", (req, res) => {
  httpGetForProxy(STORAGE_API_URL + "/components/list", req, res);
});

router.get("/components/:type", (req, res) => {
  let url = STORAGE_API_URL + "/components";
  switch (req.params.type) {
    case "interface":
      url += "/interface?";
      break;
    case "definition":
      url += "/definition?";
      break;
    case "effective":
      url += "/effective-configuration?";
      break;
    case "config":
      url += "/configuration?";
      break;
    case "configTemplate":
      url += "/configuration-template?";
      break;
    case "configDescription":
      url += "/configuration-description?";
      break;
    case "dialog":
      url += "/dialog?";
      break;
    default:
      res.status(HTTP.NOT_FOUND);
      return;
  }
  httpGetForProxy(url, req, res);
});

router.post("/components", (req, res) => {
  httpPostForProxy(STORAGE_API_URL + "/components/", req, res);
});

router.delete("/components", (req, res) => {
  httpDeleteForProxy(STORAGE_API_URL + "/components", req, res);
});

router.post("/components/config", (req, res) => {
  httpPutForProxy(STORAGE_API_URL + "/components/configuration", req, res);
});

router.post("/components/component", (req, res) => {
  httpPutForProxy(STORAGE_API_URL + "/components/component", req, res);
});

// Pipeline

router.get("/pipelines-list", (req, res) => {
  httpGetForProxy(STORAGE_API_URL + "/pipelines/list", req, res);
});

router.get("/pipelines", function (req, res) {
  httpGetForProxy(STORAGE_API_URL + "/pipelines", req, res);
});

router.delete("/pipelines", (req, res) => {
  httpDeleteForProxy(STORAGE_API_URL + "/pipelines", req, res);
});

router.put("/pipelines", (req, res) => {
  handleUpdatePipeline(req, res).catch(error => {
    console.error("Can't update pipeline.", error);
    res.status(HTTP.SERVER_ERROR).json({
      "error": {
        "message": "Can't execute request."
      }
    })
  });
});

router.post("/pipelines", (req, res) => {
  httpPostForProxy(STORAGE_API_URL + "/pipelines", req, res);
});

router.post("/pipelines-copy", (req, res) => {
  handlePipelineCopy(req, res).catch(error => {
    console.error("Can't copy pipeline.", error);
    res.status(HTTP.SERVER_ERROR).json({
      "error": {
        "message": "Can't execute request."
      }
    })
  });
});

// Execution

router.get("/executions-list", (req, res) => {
  httpGetForProxy(MONITOR_API_URL + "/executions", req, res);
});

router.get("/executions", (req, res) => {
  httpGetForProxy(MONITOR_API_URL + "/executions/", req, res);
});

router.post("/executions", (req, res) => {
  handleCreateExecution(req, res).catch(error => {
    console.error("Can't create execution.", error);
    res.status(HTTP.SERVER_ERROR).json({
      "error": {
        "message": "Can't execute request."
      }
    })
  });
});

router.post("/executions-cancel", (req, res) => {
  httpPostForProxy(MONITOR_API_URL + "/executions/cancel", req, res);
});

router.delete("/executions", (req, res) => {
  httpDeleteForProxy(MONITOR_API_URL + "/executions/", req, res);
});

router.get("/executions-pipeline", (req, res) => {
  httpGetForProxy(MONITOR_API_URL + "/executions/pipeline", req, res);
});

router.get("/executions-overview", (req, res) => {
  httpGetForProxy(MONITOR_API_URL + "/executions/overview", req, res);
});

router.get("/executions-logs", (req, res) => {
  httpGetForProxy(MONITOR_API_URL + "/executions/logs", req, res);
});

router.get("/executions-logs-tail", (req, res) => {
  httpGetForProxy(MONITOR_API_URL + "/executions/logs-tail", req, res);
});

router.get("/executions-messages", (req, res) => {
  httpGetForProxy(MONITOR_API_URL + "/executions/messages", req, res);
});

// Debug

router.get("/debug/metadata/**", (req, res) => {
  let url = MONITOR_API_URL + "/debug" + req.originalUrl.replace("/api/v1/debug", "");
  httpGetForProxy(url, req, res);
});

router.get("/debug/data/**", (req, res) => {
  let url = MONITOR_API_URL + "/debug" + req.originalUrl.replace("/api/v1/debug", "");
  httpGetForProxy(url, req, res);
});

// Other

router.get("/status", (req, res) => {
  res.status(200).json({
    "instance": {
      "label": configuration.instanceLabel
    }
  });
});

router.get("/usage", (req, res) => {
  httpGetForProxy(STORAGE_API_URL + "/components/usage", req, res);
});

router.get("/export", (req, res) => {
  httpGetForProxy(STORAGE_API_URL + "/management/export", req, res);
});

router.get("/designer", (req, res) => {
  httpGetForProxy(STORAGE_API_URL + "/management/assistant", req, res);
});

router.post("/import", (req, res) => {
  handleImport(req, res).catch(error => {
    console.error("Import handler failed.", error);
    res.status(HTTP.SERVER_ERROR).json({
      "error": {
        "message": "Can't execute request."
      }
    })
  });
});

router.post("/localize", (req, res) => {
  handleLocalize(req, res).catch(error => {
    console.error("Localize handler failed.", error);
    res.status(HTTP.SERVER_ERROR).json({
      "error": {
        "message": "Can't execute request."
      }
    })
  });
});
