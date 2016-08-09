define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'fileName': '',
            'sheetFilter': '',
            'rowStart': '',
            'rowEnd': '',
            'columnStart': '',
            'columnEnd': '',
            'header': false,
            'numericParse': false,
            'skipEmptyRows': false,
            'includeSheetName': false
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-excelToCsv#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            console.log('set', resource);

            $scope.dialog.fileName = rdf.getString(resource, 'fileName');
            $scope.dialog.sheetFilter = rdf.getString(resource, 'sheetFilter');
            $scope.dialog.rowStart = rdf.getInteger(resource, 'rowStart');
            $scope.dialog.rowEnd = rdf.getInteger(resource, 'rowEnd');
            $scope.dialog.columnStart = rdf.getInteger(resource, 'columnStart');
            $scope.dialog.columnEnd = rdf.getInteger(resource, 'columnEnd');
            $scope.dialog.header = rdf.getBoolean(resource, 'header');
            $scope.dialog.numericParse = rdf.getBoolean(resource, 'numericParse');
            $scope.dialog.skipEmptyRows = rdf.getBoolean(resource, 'skipEmptyRows');
            $scope.dialog.includeSheetName = rdf.getBoolean(resource, 'includeSheetName');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'fileName', $scope.dialog.fileName);
            rdf.setString(resource, 'sheetFilter', $scope.dialog.sheetFilter);
            rdf.setInteger(resource, 'rowStart', $scope.dialog.rowStart);
            rdf.setInteger(resource, 'rowEnd', $scope.dialog.rowEnd);
            rdf.setInteger(resource, 'columnStart', $scope.dialog.columnStart);
            rdf.setInteger(resource, 'columnEnd', $scope.dialog.columnEnd);
            rdf.setBoolean(resource, 'header', $scope.dialog.header);
            rdf.setBoolean(resource, 'numericParse', $scope.dialog.numericParse);
            rdf.setBoolean(resource, 'skipEmptyRows', $scope.dialog.skipEmptyRows);
            rdf.setBoolean(resource, 'includeSheetName', $scope.dialog.includeSheetName);

            console.log('get', resource);

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
