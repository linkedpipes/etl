/**
 * Then use with Angular instead of $http, a call of $scope.$apply() must
 * be called to notify Angular about changes in scope.
 * Internal $http utilizes $q, which would do thus for us.
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
                "Content-Type": "application/ld+json"
            },
            "credentials": "same-origin"
        }).then(json);
    }


    function getJson(url) {
        return fetch(url, {"credentials": "same-origin"}).then(json);
    }

    function json(response) {
        return response.json();
    }

    function postJson(url, body) {
        return fetch(url, {
            "body": JSON.stringify(body),
            "method": "POST",
            "headers": {
                "Content-Type": "application/json"
            },
            "credentials": "same-origin"
        }).then(json);
    }

    function deleteRrequest(url) {
        return fetch(url, {"method": "DELETE", "credentials": "same-origin"});
    }

    return {
        "getJsonLd": getJsonLd,
        "getJson": getJson,
        "postJson": postJson,
        "delete": deleteRrequest
    };

});