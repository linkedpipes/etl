define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'passiveMode': false,
            'binaryMode': false,
            'keepAliveControl': 0
        };

        var RDF = rdfService.create('http://plugins.linkedpipes.com/ontology/e-ftpFiles#');

        function loadDialog() {
            RDF.setData($service.config.instance);
            var resource = RDF.secureByType('Configuration');

            $scope.dialog.passiveMode = RDF.getBoolean(
                resource, 'passiveMode');
            $scope.dialog.binaryMode = RDF.getBoolean(
                resource, 'binaryMode');
            $scope.dialog.keepAliveControl = RDF.getInteger(
                resource, 'keepAliveControl');
        };

        function saveDialog() {
            var resource = RDF.secureByType('Configuration');

            RDF.setBoolean(resource, 'passiveMode',
                $scope.dialog.passiveMode);
            RDF.setBoolean(resource, 'binaryMode',
                $scope.dialog.binaryMode);
            RDF.setInteger(resource, 'keepAliveControl',
                $scope.dialog.keepAliveControl);

            console.log($scope.dialog);
            console.log(resource);

            return RDF.getData();
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
