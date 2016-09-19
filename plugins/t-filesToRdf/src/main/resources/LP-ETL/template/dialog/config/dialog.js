define([], function () {

    const PREFIX = 'http://plugins.linkedpipes.com/ontology/t-filesToRdf#';

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
            $scope.dialog.commitSize = rdf.getInteger(resource,
                PREFIX + 'commitSize');
            $scope.dialog.mimeType = rdf.getString(resource,
                PREFIX + 'mimeType');
            if ($scope.dialog.mimeType === undefined) {
                $scope.dialog.mimeType = '';
            }
            //
            $scope.control.commitSize = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'commitSizeControl'));
            $scope.control.mimeType = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'mimeTypeControl'));
        }

        function saveDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType(PREFIX + 'Configuration');
            //
            if (!$scope.control.commitSize.forced) {
                rdf.setInteger(resource, PREFIX + 'commitSize',
                    $scope.dialog.commitSize);
            }
            if (!$scope.control.mimeType.forced) {
                rdf.setString(resource, PREFIX + 'mimeType',
                    $scope.dialog.mimeType);
            }
            //
            rdf.setIri(resource, PREFIX + 'commitSizeControl',
                $service.control.toIri($scope.control.commitSize));
            rdf.setIri(resource, PREFIX + 'mimeTypeControl',
                $service.control.toIri($scope.control.mimeType));
        }

        $service.onStore = function () {
            saveDialog();
        }

        loadDialog();
    }

    controller.$inject = ['$scope', '$service', 'services.rdf.0.0.0'];
    return controller;
});
