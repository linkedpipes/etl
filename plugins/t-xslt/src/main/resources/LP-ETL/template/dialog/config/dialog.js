define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'xslt': '',
            'extension': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-xslt#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.xslt = rdf.getString(resource, 'template');
            $scope.dialog.extension = rdf.getString(resource, 'extension');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'template', $scope.dialog.xslt);
            rdf.setString(resource, 'extension', $scope.dialog.extension);

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
