define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'host': '',
            'fileName': '',
            'targetGraph': '',
            'loadDirectory': '',
            'clearGraph': true,
            'clearLoadList': false,
            'userName': '',
            'password': '',
            'statusUpdate': 10
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/x-virtuoso#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.host = rdf.getString(resource, 'uri');
            $scope.dialog.fileName = rdf.getString(resource, 'fileName');
            $scope.dialog.targetGraph = rdf.getString(resource, 'graph');
            $scope.dialog.loadDirectory = rdf.getString(resource, 'directory');
            $scope.dialog.clearGraph = rdf.getBoolean(resource, 'clearGraph');
            $scope.dialog.clearLoadList = rdf.getBoolean(resource, 'clearSqlLoadTable');
            $scope.dialog.userName = rdf.getString(resource, 'username');
            $scope.dialog.password = rdf.getString(resource, 'password');
            $scope.dialog.statusUpdate = rdf.getInteger(resource, 'updateInterval');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'uri', $scope.dialog.host);
            rdf.setString(resource, 'fileName', $scope.dialog.fileName);
            rdf.setString(resource, 'graph', $scope.dialog.targetGraph);
            rdf.setString(resource, 'directory', $scope.dialog.loadDirectory);
            rdf.setBoolean(resource, 'clearGraph', $scope.dialog.clearGraph);
            rdf.setBoolean(resource, 'clearSqlLoadTable', $scope.dialog.clearLoadList);
            rdf.setString(resource, 'username', $scope.dialog.userName);
            rdf.setString(resource, 'password', $scope.dialog.password);
            rdf.setInteger(resource, 'updateInterval', $scope.dialog.statusUpdate);

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
    controller.$inject = ['$scope', '$service', '', '$service', 'services.rdf.0.0.0'];
    return controller;
});
