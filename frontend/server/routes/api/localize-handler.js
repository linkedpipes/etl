"use strict";

const multiparty = require("multiparty");
const request = require("request");
const configuration = require("../../configuration");

const BODY_OPTIONS = "options";

const BODY_CONTENT = "pipeline";

const HTTP_INVALID_REQUEST = 400;

const HTTP_SERVER_ERROR = 500;

const STORAGE_API_URL = configuration.storage.url + "/api/v1";

async function handleLocalize(req, res) {
  const requestContent = await readRequestBody(req);
  const options = secureOptions(requestContent);
  const content = await secureContent(req, requestContent);
  if (content === null) {
    res.status(HTTP_INVALID_REQUEST).json({
      "error": {
        "message": "Missing content to import."
      }
    });
    return;
  }
  const url = STORAGE_API_URL + "/management/localize";
  const parts = {
    "options": options,
    "content": content,
  };
  const headers = {
    "accept": req.headers["accept"] ?? "application/ld+json",
  };
  postMultipart(url, parts, headers)
    .on("error", (error) => {
      console.error("Can't send data to storage.", error);
      res.status(HTTP_SERVER_ERROR).json({
        "error": {
          "message": "Can't send data to storage."
        }
      });
    })
    .pipe(res);
}

function readRequestBody(req) {
  const form = new multiparty.Form();
  const result = {};
  return new Promise((resolve, reject) => {
    form.on("part", (part) => {
      const object = {
        "headers": part["headers"],
        "chunks": [],
        "content": Buffer.alloc(2048),
      };
      result[part.name] = object;
      part.on("data", (chunk) => object.chunks.push(chunk));
      part.on("end", () => {
        object.content = Buffer.concat(object.chunks);
        object.chunks = [];
      });
    });
    form.on("close", () => {
      resolve(result);
    });
    form.on("error", reject);
    form.parse(req);
  });
}

function secureOptions(requestContent) {
  if (requestContent[BODY_OPTIONS] === undefined) {
    return createDefaultLocalizeOptions();
  }
  const data = requestContent[BODY_OPTIONS];
  return {
    "contentType": data["headers"]["content-type"],
    "fileName": parseFileName(data),
    "value": data["content"],
  };
}

function parseFileName(data) {
  const contentDisposition = data["headers"]["content-disposition"];
  const groups = contentDisposition.match('filename="([^"]+)"');
  if (groups.length === 2) {
    return groups[1];
  } else {
    return undefined;
  }
}

function createDefaultLocalizeOptions() {
  return {
    "contentType": "application/ld+json",
    "fileName": "options.jsonld",
    "value": JSON.stringify({
      "@id": "http://localhost/options",
      "@type": "http://linkedpipes.com/ontology/UpdateOptions"
    }),
  };
}

/**
 * Content to import may be given as part of the request or
 */
async function secureContent(req, requestContent) {
  if (requestContent[BODY_CONTENT] !== undefined) {
    const data = requestContent[BODY_CONTENT];
    return {
      "contentType": data["headers"]["content-type"],
      "fileName": parseFileName(data),
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

function postMultipart(url, parts, headers) {
  const formData = {};
  for (const [name, item] of Object.entries(parts)) {
    if (item === null) {
      continue;
    }
    formData[name] = {
      "value": item["value"],
      "options": {
        "contentType": item["contentType"],
        "filename": item["fileName"],
      },
    };
  }
  const options = {
    "url": url,
    "headers": headers,
    "formData": formData
  };
  return request.post(options);
}

module.exports = {
  "handleLocalize": handleLocalize,
};
