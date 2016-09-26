define([], function () {

    const PREFIX = 'http://plugins.linkedpipes.com/ontology/e-httpGetFile#';

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
            $scope.dialog.uri = rdf.getString(resource,
                PREFIX + 'fileUri');
            $scope.dialog.fileName = rdf.getString(resource,
                PREFIX + 'fileName');
            $scope.dialog.hardRedirect = rdf.getBoolean(resource,
                PREFIX + 'hardRedirect');
            //
            $scope.control.uri = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'fileUriControl'));
            $scope.control.fileName = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'fileNameControl'));
            $scope.control.hardRedirect = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'hardRedirectControl'));
        }

        function saveDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType(PREFIX + 'Configuration');
            //
            if (!$scope.control.uri) {
                rdf.setString(resource, PREFIX + 'fileUri',
                    $scope.dialog.uri);
            }
            if (!$scope.control.fileName) {
                rdf.setString(resource, PREFIX + 'fileName',
                    $scope.dialog.fileName);
            }
            if (!$scope.control.hardRedirect) {
                rdf.setBoolean(resource, PREFIX + 'hardRedirect',
                    $scope.dialog.hardRedirect);
            }
            //
            rdf.setIri(resource, PREFIX + 'fileUriControl',
                $service.control.toIri($scope.control.uri));
            rdf.setIri(resource, PREFIX + 'fileNameControl',
                $service.control.toIri($scope.control.fileName));
            rdf.setIri(resource, PREFIX + 'hardRedirectControl',
                $service.control.toIri($scope.control.hardRedirect));
        }

        $service.onStore = function () {
            saveDialog();
        }

        loadDialog();

    }

    controller.$inject = ['$scope', '$service', 'services.rdf.0.0.0'];
    return controller;
});
