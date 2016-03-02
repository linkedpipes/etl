define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'endpoint': '',
            'targetGraphURI': '',
            'clearGraph': true,
            'commitSize': '',
            'useAuthentification': false,
            'userName': '',
            'password': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/l-sparqlEndpoint#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');
            $scope.dialog.endpoint = rdf.getString(resource, 'endpoint');
            $scope.dialog.targetGraphURI = rdf.getString(resource, 'targetGraphURI');
            $scope.dialog.clearGraph = rdf.getBoolean(resource, 'clearGraph');
            $scope.dialog.commitSize = rdf.getInteger(resource, 'commitSize');
            $scope.dialog.useAuthentification = rdf.getBoolean(resource, 'useAuthentification');
            $scope.dialog.userName = rdf.getString(resource, 'userName');
            $scope.dialog.password = rdf.getString(resource, 'password');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');
            rdf.setString(resource, 'endpoint', $scope.dialog.endpoint);
            rdf.setBoolean(resource, 'targetGraphURI', $scope.dialog.targetGraphURI);
            rdf.setString(resource, 'clearGraph', $scope.dialog.clearGraph);
            rdf.setInteger(resource, 'commitSize', $scope.dialog.commitSize);
            rdf.setBoolean(resource, 'useAuthentification', $scope.dialog.useAuthentification);
            rdf.setString(resource, 'userName', $scope.dialog.userName);
            rdf.setString(resource, 'password', $scope.dialog.password);
            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});