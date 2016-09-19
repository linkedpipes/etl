define([], function () {

    const PREFIX = 'http://plugins.linkedpipes.com/ontology/t-modifyDate#';

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
            $scope.dialog.input = rdf.getString(resource, PREFIX + 'input');
            $scope.dialog.shiftBy = rdf.getInteger(resource, PREFIX + 'shiftBy');
            $scope.dialog.output = rdf.getString(resource, PREFIX + 'output');
            //
            $scope.control.input = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'inputControl'));
            $scope.control.shiftBy = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'shiftByControl'));
            $scope.control.output = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'outputControl'));
        }

        function saveDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType(PREFIX + 'Configuration');
            //
            if (!$scope.control.input.forced) {
                rdf.setString(resource, PREFIX + 'input',
                    $scope.dialog.input);
            }
            if (!$scope.control.shiftBy.forced) {
                rdf.setInteger(resource, PREFIX + 'shiftBy',
                    $scope.dialog.shiftBy);
            }
            if (!$scope.control.output.forced) {
                rdf.setString(resource, PREFIX + 'output',
                    $scope.dialog.output);
            }
            //
            rdf.setIri(resource, PREFIX + 'inputControl',
                $service.control.toIri($scope.control.input));
            rdf.setIri(resource, PREFIX + 'shiftByControl',
                $service.control.toIri($scope.control.shiftBy));
            rdf.setIri(resource, PREFIX + 'outputControl',
                $service.control.toIri($scope.control.output));
        }

        $service.onStore = function () {
            saveDialog();
        }

        loadDialog();
    }

    controller.$inject = ['$scope', '$service', 'services.rdf.0.0.0'];
    return controller;
});
