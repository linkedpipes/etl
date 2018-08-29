"use strict";

const request = require("request"); // https://github.com/request/request
const executionFactory = require("./../execution-factory");

const config = require("../../modules/configuration");
const errors = require("../error-codes");

const router = require("express").Router();
module.exports = router;

const monitorApiUrl = config.executor.monitor.url + "executions";

router.get("", (req, res) => {
    let uri = monitorApiUrl + "/?";
    if (req.query["changedSince"]) {
        uri += "changedSince=" + req.query["changedSince"];
    }
    pipeGet(uri, res);
});

function pipeGet(uri, res) {
    request.get(uri)
        .on("error", (error) => handleError(res, error))
        .pipe(res);
}

function handleError(res, error) {
    console.error("Request failed:\n", error);
    console.trace();
    res.status(503).json({"error": {"type": errors.CONNECTION}});
}


router.get("/:id", (req, res) => {
    const uri = monitorApiUrl + "/" + req.params["id"];
    pipeGet(uri, res);
});

router.post("/:id/cancel", (req, res) => {
    const url = monitorApiUrl + "/" + req.params["id"] + "/cancel";
    req.pipe(request.post(url))
        .on("error", (error) => handleError(res, error))
        .pipe(res);
});

router.delete("/:id", (req, res) => {
    const uri = monitorApiUrl + "/" + req.params["id"];
    request.del(uri)
        .on("error", (error) => handleError(res, error))
        .pipe(res);
});

router.get("/:id/pipeline", (req, res) => {
    const uri = monitorApiUrl + "/" + req.params["id"] + "/pipeline";
    pipeGet(uri, res);
});

router.get("/:id/overview", (req, res) => {
    const uri = monitorApiUrl + "/" + req.params["id"] + "/overview";
    pipeGet(uri, res);
});

router.get("/:id/logs", (req, res) => {
    const uri = monitorApiUrl + "/" + req.params["id"] + "/logs";
    pipeGet(uri, res);
});

router.get("/:id/logs-tail", (req, res) => {
    let uri = monitorApiUrl + "/" + req.params["id"] + "/logs-tail";
    if (req.query["n"] !== undefined) {
        uri += "?n=" + req.query["n"];
    }
    pipeGet(uri, res);
});

router.post('', (req, res) => {
    executionFactory.create(req, res);
});