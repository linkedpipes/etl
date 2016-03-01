define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'endpointSelect': '',
            'endpointUpdate': '',
            'endpointCRUD': '',
            'targetGraphURI': '',
            'repositoryType': 'Virtuoso',
            'useAuthentification': false,
            'userName': '',
            'password': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/l-graphStoreProtocol#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');
            $scope.dialog.endpointSelect = rdf.getString(resource, 'endpointSelect');
            $scope.dialog.endpointUpdate = rdf.getString(resource, 'endpointUpdate');
            $scope.dialog.endpointCRUD = rdf.getString(resource, 'endpointCRUD');
            $scope.dialog.targetGraphURI = rdf.getString(resource, 'targetGraphURI');
            $scope.dialog.repositoryType = rdf.getString(resource, 'repositoryType');
            $scope.dialog.useAuthentification = rdf.getBoolean(resource, 'useAuthentification');
            $scope.dialog.userName = rdf.getString(resource, 'userName');
            $scope.dialog.password = rdf.getString(resource, 'password');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');
            rdf.setString(resource, 'endpointSelect', $scope.dialog.endpointSelect);
            rdf.setString(resource, 'endpointUpdate', $scope.dialog.endpointUpdate);
            rdf.setString(resource, 'endpointCRUD', $scope.dialog.endpointCRUD);
            rdf.setString(resource, 'targetGraphURI', $scope.dialog.targetGraphURI);
            rdf.setString(resource, 'repositoryType', $scope.dialog.repositoryType);
            rdf.setBooealn(resource, 'useAuthentification', $scope.dialog.useAuthentification);
            rdf.setString(resource, 'userName', $scope.dialog.userName);
            rdf.setString(resource, 'password', $scope.dialog.password);
            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});