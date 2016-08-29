define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'commitSize': '',
            'mimeType': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-filesToRdf#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.commitSize = rdf.getInteger(resource, 'commitSize');
            $scope.dialog.mimeType = rdf.getString(resource, 'mimeType');
            if ($scope.dialog.mimeType === undefined) {
                $scope.dialog.mimeType = "";
            }
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setInteger(resource, 'commitSize', $scope.dialog.commitSize);
            rdf.setString(resource, 'mimeType', $scope.dialog.mimeType);

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
