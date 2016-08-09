define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'class': '',
            'template': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-mustache#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.class = rdf.getString(resource, 'class');
            $scope.dialog.template = rdf.getString(resource, 'template');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'class', $scope.dialog.class);
            rdf.setString(resource, 'template', $scope.dialog.template);

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
