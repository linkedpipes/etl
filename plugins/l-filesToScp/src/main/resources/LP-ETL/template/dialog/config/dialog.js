define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'host': '',
            'port': 22,
            'directory': '',
            'createDirectory': false,
            'userName': '',
            'password': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/l-filesToScp#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.host = rdf.getString(resource, 'host');
            $scope.dialog.port = rdf.getInteger(resource, 'port');
            $scope.dialog.directory = rdf.getString(resource, 'targetDirectory');
            $scope.dialog.createDirectory = rdf.getBoolean(resource, 'createDirectory');
            $scope.dialog.userName = rdf.getString(resource, 'userName');
            $scope.dialog.password = rdf.getString(resource, 'password');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'host', $scope.dialog.host);
            rdf.setInteger(resource, 'port', $scope.dialog.port);
            rdf.setString(resource, 'targetDirectory', $scope.dialog.directory);
            rdf.setBoolean(resource, 'createDirectory', $scope.dialog.createDirectory);
            rdf.setString(resource, 'userName', $scope.dialog.userName);
            rdf.setString(resource, 'password', $scope.dialog.password);

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
