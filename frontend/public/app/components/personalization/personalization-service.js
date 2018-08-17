((definition) => {
    if (typeof define === "function" && define.amd) {
        define([
            "./personalization"
        ], definition);
    }
})((_personalization) => {

    function factory($personalization, statusService) {

        let $scope;

        function initialize(scope) {
            $scope = scope;
            $scope.landingPage = $personalization.getLandingPage();
            $scope.initialListSize = $personalization.getListSize();
        }

        function save() {
            $personalization.setLandingPage($scope.landingPage);
            $personalization.setListSize($scope.initialListSize);
            statusService.success({
                "title": "Changes saved."
            });
        }

        return {
            "initialize": initialize,
            "save": save
        };
    }

    factory.$inject = [
        "personalization",
        "services.status"
    ];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        _personalization(app);
        app.factory("personalization.service", factory);
    }
});





