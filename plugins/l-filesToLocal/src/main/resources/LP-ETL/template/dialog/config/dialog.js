define([], function () {

    const PREFIX = 'http://plugins.linkedpipes.com/ontology/l-filesToLocal#';

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
            $scope.dialog.path = rdf.getString(resource, PREFIX + 'path');
            //
            $scope.control.path = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'pathControl'));
        };

        function saveDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType(PREFIX + 'Configuration');
            //
            if (!$scope.control.path.forced) {
                rdf.setString(resource, PREFIX + 'path', $scope.dialog.path);
            }
            //
            rdf.setIri(resource, PREFIX + 'pathControl',
                $service.control.toIri($scope.control.path));
        };

        $service.onStore = function () {
            saveDialog();
        }

        loadDialog();
    }

    controller.$inject = ['$scope', '$service', 'services.rdf.0.0.0'];
    return controller;
});
