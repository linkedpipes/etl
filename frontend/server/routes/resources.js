"use strict";

const express = require("express");
const cors = require("cors");

const configuration = require("../configuration");

const {
  httpGetForProxy,
  httpPostForProxy,
  httpDeleteForProxy
} = require("./handlers/http-request");
const {handleUpdatePipeline} = require("./handlers/update-pipeline-handler");
const {HTTP} = require("./handlers/http-codes");
const {handleCreateExecution} = require("./handlers/create-execution-handler");

const STORAGE_API_URL = configuration.storage.url + "/api/v1";

const MONITOR_API_URL = configuration.executor.monitor.url + "/api/v1";

const router = express.Router();
module.exports = router;

router.options('/components', cors());
router.get("/components", cors(), (req, res) => {
  httpGetForProxy(STORAGE_API_URL + "/components/list", req, res);
});

router.get("/executions", (req, res) => {
  httpGetForProxy(MONITOR_API_URL + "/executions", req.headers, res);
});

router.get("/executions/:id", (req, res) => {
  const execution = configuration.storage.domain + "/resources" + req.url;
  httpGetForProxy(MONITOR_API_URL + "/executions/", req, res,
    {"query": {"iri": execution}});
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

router.options('/pipelines', cors());
router.get("/pipelines", cors(), (req, res) => {
  httpGetForProxy(STORAGE_API_URL + "/pipelines/list", req, res);
});

router.options('/pipelines/:id', cors());
router.get("/pipelines/:id", cors(), (req, res) => {
  const pipeline = configuration.storage.domain + "/resources" + req.url;
  const query = {
    ...req.query,
    "iri": pipeline,
  };
  httpGetForProxy(STORAGE_API_URL + "/pipelines", req, res, {query});
});

router.post("/pipelines", (req, res) => {
  // We ignore the identifier.
  httpPostForProxy(STORAGE_API_URL + "/pipelines", req, res);
});

router.put("/pipelines/:id", (req, res) => {
  // We ignore the identifier.
  handleUpdatePipeline(req, res).catch(error => {
    console.error("Can't update pipeline.", error);
    res.status(HTTP.SERVER_ERROR).json({
      "error": {
        "message": "Can't execute request."
      }
    })
  });
});

router.delete("/pipelines/:id", (req, res) => {
  const pipeline = configuration.storage.domain + "/resources" + req.url;
  const query = {
    ...req.query,
    "iri": pipeline,
  };
  httpDeleteForProxy(STORAGE_API_URL + "/pipelines", req, res, {query});
});
