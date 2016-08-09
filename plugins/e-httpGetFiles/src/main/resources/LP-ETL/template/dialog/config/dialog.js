define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'hardRedirect': true,
            'skipOnError' : false
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/e-httpGetFiles#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.hardRedirect = rdf.getBoolean(resource, 'hardRedirect');
            $scope.dialog.skipOnError = rdf.getBoolean(resource, 'skipOnError');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setBoolean(resource, 'hardRedirect', $scope.dialog.hardRedirect);
            rdf.setBoolean(resource, 'skipOnError', $scope.dialog.skipOnError);

            return rdf.getData();
        };

        // Define the save function.
        $service.onStore = function () {
            saveDialog();
        }

        // Load data.
        loadDialog();

    }
    //
    controller.$inject = ['$scope', '$service', 'services.rdf.0.0.0'];
    return controller;
});
