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

    function showErrorNotification(message) {
        console.log("Error:", message);
        $notification.error({
            "title": message.title,
            "message": getMessageText(message),
            "delay": 4000
        });
    }

    function getMessageText(message) {
        if (message.response) {
            var responseData = message.response.data;
            if (responseData && responseData.exception) {
                return responseData.exception.userMessage;
            } else {
                return message.response.statusText;
            }
        } else if (message.message) {
            return message.message;
        }
    }

    function showSuccessNotification(message) {
        $notification.success({
            "title": message.title,
            "delay": 600
        });
    }

    function success(message) {
        showSuccessNotification(message);
    }

    function error(message) {
        showErrorNotification(message);
    }

    function httpPostFailed(message) {
        showErrorNotification(message);
    }

    function httpPutFailed(message) {
        showErrorNotification(message);
    }

    function httpGetFailed(message) {
        showErrorNotification(message);
    }

    function httpDeleteFailed(message) {
        showErrorNotification(message);
    }

    //
    //

    let $notification;

    function factory(_Notification) {
        $notification = _Notification;
        return {
            "success": success,
            "error": error,
            "httpPostFailed": httpPostFailed,
            "httpPutFailed": httpPutFailed,
            "httpGetFailed": httpGetFailed,
            "httpDeleteFailed": httpDeleteFailed
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
