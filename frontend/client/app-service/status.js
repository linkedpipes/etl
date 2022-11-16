((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    }
})(() => {
    "use strict";

    function success(actionLabel) {
        $notification.success({
            "title": actionLabel,
            "delay": 800
        });
    }

    function error(actionLabel, error) {
        // Check if it's not an HTTP error.
        if (error["data"] && error["data"]["error"] && error["status"]) {
            httpError(actionLabel, error);
            return;
        }
        console.error("Error: ", actionLabel, error);
        $notification.error({
            "title": actionLabel,
            "message": error ? error.message : "",
            "delay": 8000,
            "closeOnClick": false
        });
    }

    function httpError(actionLabel, response) {
        console.error("HTTP error: ", actionLabel, response);
        //
        let message;
        const error = response["data"]["error"];
        if (error) {
            switch (error["source"]) {
                case "FRONTEND":
                    message = prepareFromFrontendErrorMessage(error);
                    break;
                default:
                    message = prepareFromErrorMessage(error);
                    break;
            }
        } else {
            message = response.statusText;
        }
        $notification.error({
            "title": actionLabel,
            "delay": 10000,
            "message": message,
            "closeOnClick": false
        });
    }

    function prepareFromFrontendErrorMessage(error) {
        return prepareFromErrorMessage(error);
    }

    function prepareFromErrorMessage(error) {
        let errorMessage = error["message"] ? error["message"] : "";
        //
        let output = "Message: " + errorMessage + "<br/>";
        if (error["source"]) {
            output += "Source: " + error["source"].toLowerCase() + "<br/>";
        }
        if (error["status"]) {
            output += "Status: " + error["status"].toLowerCase() + "<br/>";
        }
        if (error["cause"]) {
            output += "Cause: " + error["cause"];
        }
        return output;
    }

    let $notification;

    function factory(notification) {
        $notification = notification;
        return {
            "success": success,
            "error": error,
            // TODO Remove and use only one report function and check for props.
            "httpError": httpError
        }
    }

    factory.$inject = ["Notification"];

    let _initialized = false;
    return function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        app.factory("status", factory);
    };

});
