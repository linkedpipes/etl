export const ERROR_OFFLINE = "OFFLINE";
export const ERROR_PARSING = "PARSING";
export const ERROR_RESPONSE = "RESPONSE";

/**
 * Return object with properties: json, status, error.
 */
export function fetchJson(url) {
  return requestJson("GET", url);
}

function requestJson(method, url, content) {
  const request = {
    "method": method,
    "headers": {
      "Accept": "application/json"
    }
  };
  if (content !== undefined) {
    request["body"] = JSON.stringify(content);
    request["headers"]["Content-Type"] = "application/json";
  }
  return fetch(url, request)
    .catch(failureToResponse)
    .then(handleJsonRequest);
}

function handleJsonRequest(response) {
  let promise;
  const contentType = response.headers.get("content-type");
  if (contentType) {
    promise = response.json().catch(() => {
      return Promise.reject({
        "error": ERROR_PARSING,
        "content-type": contentType
      })
    });
  } else {
    promise = Promise.resolve({});
  }
  return promise.then((data) => {
    if (response.ok) {
      return {
        "status": response.status,
        "json": data
      }
    } else {
      return Promise.reject({
        "error": ERROR_RESPONSE,
        "status": response.status,
        "json": data
      });
    }
  }).catch((error) => {
    throw error;
  })
}

function failureToResponse(error) {
  console.error("Can't execute fetch: ", error);
  return Promise.reject({
    "error": ERROR_OFFLINE
  })
}

export function fetchPlainText(url) {
  return requestPlainText("GET", url);
}

function requestPlainText(method, url) {
  const request = {
    "method": method,
    "headers": {}
  };
  return fetch(url, request)
    .catch(failureToResponse)
    .then(handlePlainTextRequest);
}

function handlePlainTextRequest(response) {
  const contentType = response.headers.get("content-type");
  let promise = response.text().catch(() => {
    return Promise.reject({
      "error": ERROR_PARSING,
      "content-type": contentType
    })
  });
  return promise.then((text) => {
    if (response.ok) {
      return {
        "status": response.status,
        "content-type": contentType,
        "text": text
      }
    } else {
      return Promise.reject({
        "error": ERROR_RESPONSE,
        "status": response.status,
        "content-type": contentType,
        "text": text
      });
    }
  })
}
