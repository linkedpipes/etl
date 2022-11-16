"use strict";

const request = require("request");

const {HTTP} = require("./http-codes");

async function httpGetContentJsonLd(url) {
  return httpGetContent(url, "application/ld+json");
}

async function httpGetContent(url, accept) {
  const options = {
    "headers": {
      "accept": accept,
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
    });
  });
}

/**
 * Each part must contain: value, contentType and fileName.
 */
function httpPostContent(url, parts, headers) {
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

function httpGetForProxy(url, req, res, extra = {}) {
  const options = {
    "url": url,
    "headers": extra.headers ?? req.headers,
    "qs": extra.query ?? req.query,
  };
  request.get(options)
    .on("error", (error) => handleConnectionError(url, error, res))
    .pipe(res);
}

function handleConnectionError(url, error, res) {
  console.error(`Request to '${url}' failed:\n`, error);
  res.status(HTTP.SERVER_ERROR).json({
    "error": {
      "type": "errors.CONNECTION",
      "source": "FRONTEND",
    },
  });
}

function httpPostForProxy(url, req, res) {
  req.pipe(request.post(url, {"form": req.body}), {"end": false})
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
}

function httpDeleteForProxy(url, req, res, extra = {}) {
  const options = {
    "url": url,
    "headers": extra.headers ?? req.headers,
    "qs": extra.query ?? req.query,
  };
  request.delete(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
}

function httpPutContent(url, req, res, extra = {}) {
  const options = {
    "url": url,
    "qs": req.query,
    "formData": extra?.formData,
  };
  request.put(options)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
}

function httpPutForProxy(url, req, res) {
  const options = {
    "url": url,
    "qs": req.query,
    "form": req.body
  };
  req.pipe(request.put(options), {"end": false})
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
}

module.exports = {
  "httpGetContentJsonLd": httpGetContentJsonLd,
  "httpGetContent": httpGetContent,
  "httpPostContent": httpPostContent,
  "httpGetForProxy": httpGetForProxy,
  "httpPostForProxy": httpPostForProxy,
  "httpDeleteForProxy": httpDeleteForProxy,
  "httpPutContent": httpPutContent,
  "httpPutForProxy": httpPutForProxy,
};
