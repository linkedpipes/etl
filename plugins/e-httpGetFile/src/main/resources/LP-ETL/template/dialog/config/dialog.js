define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'fileUri': '',
            'fileName': '',
            'hardRedirect': false
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/e-httpGetFile#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.uri = rdf.getString(resource, 'fileUri');
            $scope.dialog.fileName = rdf.getString(resource, 'fileName');
            $scope.dialog.hardRedirect = rdf.getBoolean(resource, 'hardRedirect');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'fileUri', $scope.dialog.uri);
            rdf.setString(resource, 'fileName', $scope.dialog.fileName);
            rdf.setBoolean(resource, 'hardRedirect', $scope.dialog.hardRedirect);

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
