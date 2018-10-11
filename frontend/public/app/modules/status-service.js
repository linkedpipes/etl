/**
 * See link for description of Notification service:
 *  https://github.com/alexcrack/angular-ui-notification
 *
 *  TODO Rename to status.js
 */
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
        console.log("Error: ", actionLabel, error);
        $notification.error({
            "title": actionLabel,
            "message": error.message,
            "delay": 8000,
            "closeOnClick": false
        });
    }

    function httpError(actionLabel, response) {
        console.log("HTTP error: ", actionLabel, response);
        //
        const error = response["data"]["error"];
        let message;
        switch (error["source"]) {
            case "FRONTEND":
                message = prepareFromFrontendErrorMessage(error);
                break;
            default:
                message = prepareFromErrorMessage(error);
                break;
        }
        $notification.error({
            "title": actionLabel,
            "delay": 10000,
            "message": message,
            "closeOnClick": false
        });
    }

    function prepareFromFrontendErrorMessage(error) {
        switch (error["type"]) {
            case "CONNECTION":
                return "Backend service is offline.";
            case "INVALID_REQUEST":
                return "Invalid request. <br/> Message: " + error["message"];
            case "MISSING":
                if (error["message"]) {
                    return "Message: " + error["message"];
                } else {
                    return "Missing resource."
                }
            case "ERROR":
                // The error message have same structure.
                return prepareFromErrorMessage(error);
            default:
                return "Undefined error type: " + error["type"] + "</br>" +
                    JSON.stringify(error);
        }
    }

    function prepareFromErrorMessage(error) {
        let message = "Message: " + error["message"] + "<br/>";
        message += "Source: " + error["source"].toLowerCase() + "<br/>";
        if (error["status"]) {
            message += "Status: " + error["status"].toLowerCase() + "<br/>";
        }
        if (error["cause"]) {
            message += "Cause: " + error["cause"];
        }
        return message;
    }

    let $notification;
    let $scope;

    function factory(notification) {
        $notification = notification;
        return {
            "success": success,
            "error": error,
            "httpError": httpError,

            "httpPostFailed": null,
            "httpPutFailed": null,
            "httpGetFailed": null,
            "httpDeleteFailed": null
        }
    }

    factory.$inject = ["Notification"];

    let _initialized = false;
    return function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        app.factory("services.status", factory);
    };

});
