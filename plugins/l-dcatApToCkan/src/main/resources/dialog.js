define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'apiUrl': '',
            'apiKey': '',
            'loadToCKAN': true,
            'datasetID': '',
            'filename': '',
            'orgID': '',
            'loadLanguage': '',
            'generateVirtuoso': true,
            'generateExampleResource': true,
            'overwrite': true
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/l-dcatApToCkan#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.apiUrl = rdf.getString(resource, 'apiUrl');
            $scope.dialog.apiKey = rdf.getString(resource, 'apiKey');
            $scope.dialog.loadToCKAN = rdf.getBoolean(resource, 'loadToCKAN');
            $scope.dialog.datasetID = rdf.getString(resource, 'datasetID');
            $scope.dialog.filename = rdf.getString(resource, 'filename');
            $scope.dialog.orgID = rdf.getString(resource, 'organizationId');
            $scope.dialog.loadLanguage = rdf.getString(resource, 'loadLanguage');
            $scope.dialog.generateVirtuoso = rdf.getBoolean(resource, 'generateVirtuosoExample');
            $scope.dialog.generateExampleResource = rdf.getBoolean(resource, 'generateExample');
            $scope.dialog.overwrite = rdf.getBoolean(resource, 'overwrite');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'apiUrl', $scope.dialog.apiUrl);
            rdf.setString(resource, 'apiKey', $scope.dialog.apiKey);
            rdf.setBoolean(resource, 'loadToCKAN', $scope.dialog.loadToCKAN);
            rdf.setString(resource, 'datasetID', $scope.dialog.datasetID);
            rdf.setString(resource, 'filename', $scope.dialog.filename);
            rdf.setString(resource, 'organizationId', $scope.dialog.orgID);
            rdf.setString(resource, 'loadLanguage', $scope.dialog.loadLanguage);
            rdf.setBoolean(resource, 'generateVirtuosoExample', $scope.dialog.generateVirtuoso);
            rdf.setBoolean(resource, 'generateExample', $scope.dialog.generateExampleResource);
            rdf.setBoolean(resource, 'overwrite', $scope.dialog.overwrite);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});