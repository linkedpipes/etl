define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'graph': '',
            'repository': 'VIRTUOSO',
            'authentification': false,
            'user': '',
            'password': '',
            'checkSize': false,
            'endpointSelect': '',
            'endpoint': '',
            'replace': false
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/l-graphStoreProtocol#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');
            $scope.dialog.graph = rdf.getString(resource, 'graph');
            $scope.dialog.repository = rdf.getString(resource, 'repository');
            $scope.dialog.authentification = rdf.getString(resource, 'authentification');
            $scope.dialog.user = rdf.getString(resource, 'user');
            $scope.dialog.password = rdf.getString(resource, 'password');
            $scope.dialog.checkSize = rdf.getString(resource, 'checkSize');
            $scope.dialog.endpointSelect = rdf.getString(resource, 'endpointSelect');
            $scope.dialog.endpoint = rdf.getString(resource, 'endpoint');
            $scope.dialog.replace = rdf.getBoolean(resource, 'replace');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');
            rdf.setString(resource, 'graph', $scope.dialog.graph);
            rdf.setString(resource, 'repository', $scope.dialog.repository);
            rdf.setBoolean(resource, 'authentification', $scope.dialog.authentification);
            rdf.setString(resource, 'user', $scope.dialog.user);
            rdf.setString(resource, 'password', $scope.dialog.password);
            rdf.setBoolean(resource, 'checkSize', $scope.dialog.checkSize);
            rdf.setString(resource, 'endpointSelect', $scope.dialog.endpointSelect);
            rdf.setString(resource, 'endpoint', $scope.dialog.endpoint);
            rdf.setBoolean(resource, 'replace', $scope.dialog.replace);
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
