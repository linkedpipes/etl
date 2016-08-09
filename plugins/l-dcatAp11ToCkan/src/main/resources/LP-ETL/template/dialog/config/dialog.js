define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = { };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/l-dcatAp11ToCkan#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.apiUrl = rdf.getString(resource, 'apiUrl');
            $scope.dialog.apiKey = rdf.getString(resource, 'apiKey');
            $scope.dialog.createCkanOrg = rdf.getBoolean(resource, 'createCkanOrg');
            $scope.dialog.datasetID = rdf.getString(resource, 'datasetID');
            $scope.dialog.orgID = rdf.getString(resource, 'organizationId');
            $scope.dialog.loadLanguage = rdf.getString(resource, 'loadLanguage') ;
            $scope.dialog.overwrite = rdf.getBoolean(resource, 'overwrite');
        };

        function saveDialog() {
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
