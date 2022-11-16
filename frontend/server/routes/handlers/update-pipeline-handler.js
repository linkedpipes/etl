"use strict";

const configuration = require("../../configuration");

const {guessFileName} = require("./request-parser");
const {httpPutContent} = require("./http-request");

const STORAGE_API_URL = configuration.storage.url + "/api/v1";

async function handleUpdatePipeline(req, res) {
  const formData = {
    "pipeline": {
      "value": req,
      "options": {
        "contentType": req.get("content-type"),
        "filename": guessFileName("pipeline", req.headers),
      }
    }
  };
  httpPutContent(STORAGE_API_URL + "/pipelines", req, res,
    {"formData": formData});
}

module.exports = {
  "handleUpdatePipeline": handleUpdatePipeline,
};
