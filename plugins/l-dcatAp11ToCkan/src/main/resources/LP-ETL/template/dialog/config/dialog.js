define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = { };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/l-dcatAp11ToCkan#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.apiUrl = rdf.getString(resource, 'apiUrl');
            $scope.dialog.apiKey = rdf.getString(resource, 'apiKey');
            $scope.dialog.createCkanOrg = rdf.getBoolean(resource, 'createCkanOrg');
            $scope.dialog.datasetID = rdf.getString(resource, 'datasetID');
            $scope.dialog.orgID = rdf.getString(resource, 'organizationId');
            $scope.dialog.loadLanguage = rdf.getString(resource, 'loadLanguage') ;
            $scope.dialog.overwrite = rdf.getBoolean(resource, 'overwrite');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'apiUrl', $scope.dialog.apiUrl);
            rdf.setString(resource, 'apiKey', $scope.dialog.apiKey);
            rdf.setBoolean(resource, 'createCkanOrg', $scope.dialog.createCkanOrg);
            rdf.setString(resource, 'datasetID', $scope.dialog.datasetID);
            rdf.setString(resource, 'organizationId', $scope.dialog.orgID);
            rdf.setString(resource, 'loadLanguage', $scope.dialog.loadLanguage);
            rdf.setBoolean(resource, 'overwrite', $scope.dialog.overwrite);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});