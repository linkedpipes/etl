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
            }
        }).then(json);
    }


    function getJson(url) {
        return fetch(url).then(json);
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
        }).then(json);
    }

    function deleteRrequest(url) {
        return fetch(url, {"method": "DELETE"});
    }

    return {
        "getJsonLd": getJsonLd,
        "getJson": getJson,
        "postJson": postJson,
        "delete": deleteRrequest
    };

});