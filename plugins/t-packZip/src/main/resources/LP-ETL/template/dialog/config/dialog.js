define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'fileName': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-packZip#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.fileName = rdf.getString(resource, 'fileName');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'fileName', $scope.dialog.fileName);

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
