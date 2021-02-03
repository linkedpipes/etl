/**
 * If used with Angular instead of $http, a $scope.$apply() must
 * be called to notify Angular about changes in scope.
 *
 * Internal $http utilizes $q, which would do this for us.
 */
((definition) => {
  if (typeof define === "function" && define.amd) {
    define([], definition);
  }
})(() => {
  "use strict";

  function getJsonLd(url) {
    return get(url, {"headers": {"Accept": "application/ld+json"},})
      .catch(onFetchFailed)
      .then(asJson);
  }

  function get(url, options) {
    if (options === undefined) {
      options = {};
    }
    return fetch(url, {
      "method": "GET",
      "credentials": "same-origin",
      ...options
    });
  }

  function onFetchFailed(error) {
    console.log("Fetch request failed:", error);
    return Promise.reject({
      "error": "offline"
    })
  }

  function asJson(response) {
    return response.json().catch(() => {
      return Promise.reject({
        "status": response.status,
        "error": "parsing"
      })
    }).then((data) => {
      if (response.ok) {
        return {
          "status": response.status,
          "payload": data
        }
      } else {
        return Promise.reject({
          "error": "server",
          "status": response.status,
          "payload": data
        });
      }
    });
  }

  function getJson(url) {
    return get(url)
      .then(asJson)
      .catch(onFetchFailed);
  }


  function postJson(url, body) {
    return fetch(url, {
      "body": JSON.stringify(body),
      "method": "POST",
      "headers": {
        "Accept": "application/json",
        "Content-Type": "application/json; charset=utf-8"
      },
      "credentials": "same-origin"
    }).then(asJson)
      .catch(onFetchFailed);
  }

  function deleteRequest(url) {
    return fetch(url, {
      "method": "DELETE",
      "credentials": "same-origin"
    });
  }

  return {
    "getJsonLd": getJsonLd,
    "getJson": getJson,
    "postJson": postJson,
    "delete": deleteRequest
  };

});