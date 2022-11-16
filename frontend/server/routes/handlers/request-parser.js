const multiparty = require("multiparty");

async function parseRequestBody(req, bodyName) {
  if (req.is("multipart/form-data")) {
    return parseMultipartRequestBody(req);
  } else {
    return parseRawRequestBody(req, bodyName);
  }
}

async function parseMultipartRequestBody(req) {
  const form = new multiparty.Form();
  const result = {};
  return new Promise((resolve, reject) => {
    form.on("part", (part) => {
      const headers = part["headers"];
      const object = {
        "headers": headers,
        "value": Buffer.alloc(0),
        "contentType": headers["content-type"],
        "fileName": fileNameFromContentDisposition(
          headers["content-disposition"]),
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

async function collectStreamToBuffer(source) {
  return new Promise((resolve, reject) => {
    const collector = [];
    source.on("data", (chunk) => collector.push(chunk));
    source.on("end", () => resolve(Buffer.concat(collector)));
    source.on("error", reject);
  });
}

async function parseRawRequestBody(req, bodyName) {
  return {
    [bodyName]: [{
      "headers": {
        "content-type": req.get("content-type"),
      },
      "value": await collectStreamToBuffer(req),
      "contentType": req.get("content-type"),
      "fileName": undefined,
    }],
  };
}

function guessFileName(name, entry) {
  const contentType = entry["contentType"];
  // TODO Use content to detect the extension.
  return name + ".jsonld";
}

module.exports = {
  "readRequestBody": parseRequestBody,
  "collectStreamToBuffer": collectStreamToBuffer,
  "guessFileName": guessFileName,
};
