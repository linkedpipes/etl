define([], function () {

    const PREFIX = 'http://plugins.linkedpipes.com/ontology/t-templatedXlsToCsv#';

    function controller($scope, $service, rdfService) {

        $scope.dialog = {};

        if ($scope.control === undefined) {
            $scope.control = {};
        }

        var rdf = rdfService.create();

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType(PREFIX + 'Configuration');
            //
            $scope.dialog.prefix = rdf.getString(resource, PREFIX + 'prefix');
            //
            $scope.control.prefix = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'prefixControl'));
        }

        function saveDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType(PREFIX + 'Configuration');
            //
            if (!$scope.control.prefix.forced) {
                rdf.setString(resource, PREFIX + 'prefix',
                    $scope.dialog.prefix);
            }
            //
            rdf.setIri(resource, PREFIX + 'prefixControl',
                $service.control.toIri($scope.control.prefix));
        }

        $service.onStore = function () {
            saveDialog();
        }

        loadDialog();
    }

    controller.$inject = ['$scope', '$service', 'services.rdf.0.0.0'];
    return controller;
});
