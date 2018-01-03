/**
 * See link for description of Notification service:
 *  https://github.com/alexcrack/angular-ui-notification
 */
(function () {
    "use strict";

    function showErrorNotification(message) {
        console.log("Error:", message);
        Notification.error({
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
        Notification.success({
            "title": message.title,
            "delay": 600
        });
    }

    const service = {};

    service.success = function (message) {
        showSuccessNotification(message);
    };

    service.error = function (message) {
        showErrorNotification(message);
    };

    service.httpPostFailed = function (message) {
        showErrorNotification(message);
    };

    service.httpPutFailed = function (message) {
        showErrorNotification(message);
    };

    service.httpGetFailed = function (message) {
        showErrorNotification(message);
    };

    service.httpDeleteFailed = function (message) {
        showErrorNotification(message);
    };

    //
    //

    let Notification;

    function factory(_Notification) {
        Notification = _Notification;
        return service;
    }

    factory.$inject = ["Notification"];

    let _initialized = false;
    function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        app.factory("services.status", factory);
    }

    if (typeof define === "function" && define.amd) {
        define([], () => init);
    }

})();