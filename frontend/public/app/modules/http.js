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
        return fetch(url, {
            "method": "GET",
            "headers": {
                "Accept": "application/ld+json"
            },
            "credentials": "same-origin"
        }).catch(failureToResponse).then(json);
    }

    function json(response) {
        return response.json().catch(() => {
            return Promise.reject({
                "error": "parsing"
            })
        }).then((data) => {
            if (response.ok) {
                return {
                    "status": response.status,
                    "json": data
                }
            } else {
                return Promise.reject({
                    "error": "server",
                    "status": response.status,
                    "json": data
                });
            }
        });
    }

    function failureToResponse(error) {
        console.log("Fetch request failed:", error);
        return Promise.reject({
            "error": "offline"
        })
    }

    function getJson(url) {
        return fetch(url, {
            "method": "GET",
            "credentials": "same-origin"
        }).then(json)
            .catch(failureToResponse);
    }

    function postJson(url, body) {
        return fetch(url, {
            "body": JSON.stringify(body),
            "method": "POST",
            "headers": {
                "Accept": "application/json",
                "Content-Type": "application/json"
            },
            "credentials": "same-origin"
        }).then(json)
            .catch(failureToResponse);
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