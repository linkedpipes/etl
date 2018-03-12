define([], function () {
    "use strict";

    function controller($scope, infoService) {
        fetchCommitVersion($scope, infoService);
    }

    function fetchCommitVersion($scope, infoService) {
        infoService.fetch().then((info) => {
            if (info.version !== undefined) {
                $scope.commit = info.version.commit;
            }
        });
    }

    controller.$inject = [
        "$scope",
        "service.info"
    ];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        app.controller("help", controller);
    }

});
