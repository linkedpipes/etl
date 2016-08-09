define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'path': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/e-filesFromLocal#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.path = rdf.getString(resource, 'path');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'path', $scope.dialog.path);

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
