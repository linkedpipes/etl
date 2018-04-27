((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    }
})(() => {
    "use strict";

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

    return {
        "getJson": getJson,
        "postJson": postJson
    };

});