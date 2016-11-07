define([], function () {
    "use strict";

    function controller($scope, infoService) {
        infoService.fetch().then((info) => {
            if (info.version !== undefined) {
                $scope.commit = info.version.commit;
            }
        });
    }

    let _initialized = false;
    return function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        app.controller('help', ['$scope', 'service.info',
            controller]);
    };

});
