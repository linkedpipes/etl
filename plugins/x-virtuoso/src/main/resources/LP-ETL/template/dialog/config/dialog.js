define([], function () {

    const PREFIX = 'http://plugins.linkedpipes.com/ontology/x-virtuoso#';

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
            $scope.dialog.host = rdf.getString(resource,
                PREFIX + 'uri');
            $scope.dialog.fileName = rdf.getString(resource,
                PREFIX + 'fileName');
            $scope.dialog.targetGraph = rdf.getString(resource,
                PREFIX + 'graph');
            $scope.dialog.loadDirectory = rdf.getString(resource,
                PREFIX + 'directory');
            $scope.dialog.clearGraph = rdf.getBoolean(resource,
                PREFIX + 'clearGraph');
            $scope.dialog.clearLoadList = rdf.getBoolean(resource,
                PREFIX + 'clearSqlLoadTable');
            $scope.dialog.userName = rdf.getString(resource,
                PREFIX + 'username');
            $scope.dialog.password = rdf.getString(resource,
                PREFIX + 'password');
            $scope.dialog.statusUpdate = rdf.getInteger(resource,
                PREFIX + 'updateInterval');
            //
            $scope.control.host = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'hostControl'));
            $scope.control.fileName = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'fileNameControl'));
            $scope.control.targetGraph = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'targetGraphControl'));
            $scope.control.loadDirectory = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'loadDirectoryControl'));
            $scope.control.clearGraph = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'clearGraphControl'));
            $scope.control.clearLoadList = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'clearSqlLoadTableControl'));
            $scope.control.userName = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'userNameControl'));
            $scope.control.password = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'passwordControl'));
            $scope.control.statusUpdate = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'statusUpdateControl'));
        }

        function saveDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType(PREFIX + 'Configuration');
            //
            if (!$scope.control.host.forced) {
                rdf.setString(resource, PREFIX + 'uri',
                    $scope.dialog.host);
            }
            if (!$scope.control.fileName.forced) {
                rdf.setString(resource, PREFIX + 'fileName',
                    $scope.dialog.fileName);
            }
            if (!$scope.control.targetGraph.forced) {
                rdf.setString(resource, PREFIX + 'graph',
                    $scope.dialog.targetGraph);
            }
            if (!$scope.control.loadDirectory.forced) {
                rdf.setString(resource, PREFIX + 'directory',
                    $scope.dialog.loadDirectory);
            }
            if (!$scope.control.clearGraph.forced) {
                rdf.setBoolean(resource, PREFIX + 'clearGraph',
                    $scope.dialog.clearGraph);
            }
            if (!$scope.control.clearLoadList.forced) {
                rdf.setBoolean(resource, PREFIX + 'clearSqlLoadTable',
                    $scope.dialog.clearLoadList);
            }
            if (!$scope.control.userName.forced) {
                rdf.setString(resource, PREFIX + 'username',
                    $scope.dialog.userName);
            }
            if (!$scope.control.password.forced) {
                rdf.setString(resource, PREFIX + 'password',
                    $scope.dialog.password);
            }
            if (!$scope.control.statusUpdate.forced) {
                rdf.setInteger(resource, PREFIX + 'updateInterval',
                    $scope.dialog.statusUpdate);
            }
            //
            rdf.setIri(resource, PREFIX + 'hostControl',
                $service.control.toIri($scope.control.host));
            rdf.setIri(resource, PREFIX + 'fileNameControl',
                $service.control.toIri($scope.control.fileName));
            rdf.setIri(resource, PREFIX + 'targetGraphControl',
                $service.control.toIri($scope.control.targetGraph));
            rdf.setIri(resource, PREFIX + 'loadDirectoryControl',
                $service.control.toIri($scope.control.loadDirectory));
            rdf.setIri(resource, PREFIX + 'clearGraphControl',
                $service.control.toIri($scope.control.clearGraph));
            rdf.setIri(resource, PREFIX + 'clearSqlLoadTableControl',
                $service.control.toIri($scope.control.clearLoadList));
            rdf.setIri(resource, PREFIX + 'userNameControl',
                $service.control.toIri($scope.control.userName));
            rdf.setIri(resource, PREFIX + 'passwordControl',
                $service.control.toIri($scope.control.password));
            rdf.setIri(resource, PREFIX + 'statusUpdateControl',
                $service.control.toIri($scope.control.statusUpdate));
        }

        $service.onStore = function () {
            saveDialog();
        }

        loadDialog();
    }

    controller.$inject = ['$scope', '$service', 'services.rdf.0.0.0'];
    return controller;
});
