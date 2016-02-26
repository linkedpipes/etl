/**
 * Define service that should be used for periodic update tasks. The service
 * reset on the page change.
 */
define([], function () {
    function factoryFunction(Notification) {

        // See link for description of Notification service.
        // https://github.com/alexcrack/angular-ui-notification

        var service = {
        };

        service.success = function (message) {
            Notification.success({
                'title': message.title,
                'delay': 600
            });
        };

        var showError = function(message) {
            // TODO Add better logging and response utilization.
            console.log('error.response', message.response);
            
            // Based on message format select message text.
            var messageText;
            if (message.response) {
                var responseData = message.response.data;
                if (responseData && responseData.exception) {
                    messageText = responseData.exception.userMessage;
                } else {
                    messageText = message.response.statusText;
                }
            } else if (message.message) {
                messageText = message.message;
            }

            Notification.error({
                'title': message.title,
                'message': messageText,
                'delay': 4000
            });
        };

        /**
         * Used to report application error.
         */
        service.error = function (message) {
            showError(message);
        };

        service.postFailed = function (message) {
            showError(message);
        };

        service.putFailed = function (message) {
            showError(message);
        };

        service.getFailed = function (message) {
            showError(message);
        };

        service.deleteFailed = function (message) {
            showError(message);
        };

        return service;
    }
    //
    factoryFunction.$inject = ['Notification'];
    //
    function init(app) {
        app.factory('services.status', factoryFunction);
    }
    return init;
});