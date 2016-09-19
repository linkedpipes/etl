define([], function () {

    const PREFIX = 'http://plugins.linkedpipes.com/ontology/e-sparqlEndpoint#';

    function controller($scope, $service, rdfService) {

        $scope.dialog = {};

        if ($scope.control === undefined) {
            $scope.control = {};
        }

        var rdf = rdfService.create('');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType(PREFIX + 'Configuration');
            //
            $scope.dialog.query = rdf.getString(resource,
                PREFIX + 'query');
            $scope.dialog.endpoint = rdf.getString(resource,
                PREFIX + 'endpoint');
            //
            $scope.control.query = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'queryControl'));
            $scope.control.endpoint = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'endpointControl'));
        }

        function saveDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType(PREFIX + 'Configuration');
            //
            if (!$scope.control.query.forced) {
                rdf.setString(resource, PREFIX + 'query',
                    $scope.dialog.query);
            }
            if (!$scope.control.endpoint.forced) {
                rdf.setString(resource, PREFIX + 'endpoint',
                    $scope.dialog.endpoint);
            }
            //
            rdf.setIri(resource, PREFIX + 'queryControl',
                $service.control.toIri($scope.control.query));
            rdf.setIri(resource, PREFIX + 'endpointControl',
                $service.control.toIri($scope.control.endpoint));
        }

        $service.onStore = function () {
            saveDialog();
        }

        loadDialog();

    }

    controller.$inject = ['$scope', '$service', 'services.rdf.0.0.0'];
    return controller;
});
