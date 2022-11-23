const multiparty = require("multiparty");

async function parseRequestBody(req, bodyName) {
  if (isWithoutContent(req)) {
    return {};
  } else if (isMultipartFormData(req)) {
    return parseMultipartRequestBody(req);
  } else {
    return parseRawRequestBody(req, bodyName);
  }
}

function isWithoutContent(req) {
  return req.get("content-type") === undefined;
}

function isMultipartFormData(req) {
  // We can not use req.is("multipart/form-data") as it would not pass
  // multipart/form-data; boundary==----------------------1669050523527
  let contentType = req.get("content-type") ?? "";
  return contentType.startsWith("multipart/form-data");
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
