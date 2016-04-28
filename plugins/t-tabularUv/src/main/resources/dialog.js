define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'keyColumn': '',
            'baseUri': '',
            'columns': [],
            'advancedMapping': [],
            'namedCell': [],
            'quote': '',
            'delimeter': '',
            'linesToIgnore': 0,
            'encoding': '',
            'rowsLimit': '',
            'tableType': '',
            'hasHeader': true,
            'generateNew': true,
            'ignoreBlankCell': false,
            'advancedKey': false,
            'rowClass': '',
            'sheetName': '',
            'staticRowCounter': false,
            'rowTriple': true,
            'tableSubject': false,
            'autoAsString': false,
            'tableClass': false,
            'generateLabels': false,
            'stripHeader': false,
            'trimString': false,
            'xlsAdvancedParser': false,
            'ignoreMissingColumn': false,
            'generateRowTriple': false
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-tabularUv#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var table = rdf.secureByType('Configuration');

            $scope.dialog.keyColumn = rdf.getString(table, 'keyColumn');
            $scope.dialog.baseUri = rdf.getString(table, 'baseUri');
            $scope.dialog.quote = rdf.getString(table, 'quote');
            $scope.dialog.delimeter = rdf.getString(table, 'delimeter');
            $scope.dialog.linesToIgnore = rdf.getInteger(table, 'linesToIgnore');
            $scope.dialog.encoding = rdf.getString(table, 'encoding');
            $scope.dialog.rowsLimit = rdf.getInteger(table, 'rowsLimit');
            $scope.dialog.tableType = rdf.getString(table, 'tableType');
            $scope.dialog.hasHeader = rdf.getBoolean(table, 'hasHeader');
            $scope.dialog.generateNew = rdf.getBoolean(table, 'generateNew');
            $scope.dialog.ignoreBlankCell = rdf.getBoolean(table, 'ignoreBlankCell');
            $scope.dialog.advancedKey = rdf.getBoolean(table, 'advancedKey');
            $scope.dialog.rowTriple = rdf.getBoolean(table, 'rowTriple');
            $scope.dialog.tableSubject = rdf.getBoolean(table, 'tableSubject');
            $scope.dialog.rowClass = rdf.getString(table, 'rowsClass');
            $scope.dialog.sheetName = rdf.getString(table, 'sheetName');
            $scope.dialog.staticRowCounter = rdf.getBoolean(table, 'staticRowCounter');
            $scope.dialog.rowTriple = rdf.getBoolean(table, 'rowTriple');
            $scope.dialog.tableSubject = rdf.getBoolean(table, 'tableSubject');
            $scope.dialog.autoAsString = rdf.getBoolean(table, 'autoAsString');
            $scope.dialog.tableClass = rdf.getBoolean(table, 'tableClass');
            $scope.dialog.generateLabels = rdf.getBoolean(table, 'generateLabels');
            $scope.dialog.stripHeader = rdf.getBoolean(table, 'stripHeader');
            $scope.dialog.trimString = rdf.getBoolean(table, 'trimString');
            $scope.dialog.xlsAdvancedParser = rdf.getBoolean(table, 'xlsAdvancedParser');
            $scope.dialog.ignoreMissingColumn = rdf.getBoolean(table, 'ignoreMissingColumn');
            $scope.dialog.generateRowTriple = rdf.getBoolean(table, 'generateRowTriple');

            var columns = rdf.getObjects(table, 'column');
            var newItems = [];
            columns.forEach(function(item) {
                var columnRecord = {
                    '@id' : item['@id'],
                    'name': rdf.getString(item, 'name'),
                    'uri': rdf.getString(item, 'uri'),
                    'type': rdf.getString(item, 'type'),
                    'typeFromDbf': rdf.getBoolean(item, 'typeFromDbf'),
                    'lang': rdf.getString(item, 'lang')
                };
                newItems.push(columnRecord);
            });
            $scope.dialog.columns = newItems;

            var advancedMapping = rdf.getObjects(table, 'columnsAdvanced');
            var newItems = [];
            advancedMapping.forEach(function(item) {
                var columnRecord = {
                    '@id' : item['@id'],
                    'uri': rdf.getString(item, 'uri'),
                    'template': rdf.getString(item, 'template')
                };
                newItems.push(columnRecord);
            });
            $scope.dialog.advancedMapping = newItems;

            var namedCell = rdf.getObjects(table, 'namedCell');
            var newItems = [];
            namedCell.forEach(function(item) {
                var columnRecord = {
                    '@id' : item['@id'],
                    'name': rdf.getString(item, 'name'),
                    'rowNumber': rdf.getInteger(item, 'rowNumber'),
                    'columnNumber': rdf.getInteger(item, 'columnNumber')
                };
                newItems.push(columnRecord);
            });
            $scope.dialog.namedCell = newItems;

        };

        $scope.getConfiguration = function () {
            var table = rdf.secureByType('Configuration');

            rdf.setString(table, 'keyColumn', $scope.dialog.keyColumn);
            rdf.setString(table, 'baseUri', $scope.dialog.baseUri);
            rdf.setString(table, 'quote', $scope.dialog.quote);
            rdf.setString(table, 'delimeter', $scope.dialog.delimeter);
            rdf.setInteger(table, 'linesToIgnore', $scope.dialog.linesToIgnore);
            rdf.setString(table, 'encoding', $scope.dialog.encoding);
            rdf.setInteger(table, 'rowsLimit', $scope.dialog.rowsLimit);
            rdf.setString(table, 'tableType', $scope.dialog.tableType);
            rdf.setString(table, 'hasHeader', $scope.dialog.hasHeader);
            rdf.setBoolean(table, 'generateNew', $scope.dialog.generateNew);
            rdf.setBoolean(table, 'ignoreBlankCell', $scope.dialog.ignoreBlankCell);
            rdf.setBoolean(table, 'advancedKey', $scope.dialog.advancedKey);
            rdf.setBoolean(table, 'rowTriple', $scope.dialog.rowTriple);
            rdf.setBoolean(table, 'tableSubject', $scope.dialog.tableSubject);
            rdf.setString(table, 'rowsClass', $scope.dialog.rowClass);
            rdf.setString(table, 'sheetName', $scope.dialog.sheetName);
            rdf.setBoolean(table, 'staticRowCounter', $scope.dialog.staticRowCounter);
            rdf.setBoolean(table, 'rowTriple', $scope.dialog.rowTriple);
            rdf.setBoolean(table, 'tableSubject', $scope.dialog.tableSubject);
            rdf.setBoolean(table, 'autoAsString', $scope.dialog.autoAsString);
            rdf.setBoolean(table, 'tableClass', $scope.dialog.tableClass);
            rdf.setBoolean(table, 'generateLabels', $scope.dialog.generateLabels);
            rdf.setBoolean(table, 'stripHeader', $scope.dialog.stripHeader);
            rdf.setBoolean(table, 'trimString', $scope.dialog.trimString);
            rdf.setBoolean(table, 'xlsAdvancedParser', $scope.dialog.xlsAdvancedParser);
            rdf.setBoolean(table, 'ignoreMissingColumn', $scope.dialog.ignoreMissingColumn);
            rdf.setBoolean(table, 'generateRowTriple', $scope.dialog.generateRowTriple);

            var items = [];
            $scope.dialog.columns.forEach(function(item) {
                var id = item['@id'];
                var record;
                if (id) {
                    // Try to read existing object.
                    record = rdf.filterSingle(rdf.findByUri(id));
                }
                if (!id || !record) {
                    // New object.
                    record = rdf.createObject('ColumnInfo');
                }
                // Now we have object to store data into.
                rdf.setString(record, 'name', item['name']);
                rdf.setString(record, 'uri', item['uri']);
                rdf.setString(record, 'type', item['type']);
                rdf.setBoolean(record, 'typeFromDbf', item['typeFromDbf']);
                rdf.setString(record, 'lang', item['lang']);
                items.push(record);
            });
            rdf.updateObjects(table, 'column', items, false);

            items = [];
            $scope.dialog.advancedMapping.forEach(function(item) {
                var id = item['@id'];
                var record;
                if (id) {
                    // Try to read existing object.
                    record = rdf.filterSingle(rdf.findByUri(id));
                }
                if (!id || !record) {
                    // New object.
                    record = rdf.createObject('AdvancedMapping');
                }
                // Now we have object to store data into.
                rdf.setString(record, 'uri', item['uri']);
                rdf.setString(record, 'template', item['template']);
                items.push(record);
            });
            rdf.updateObjects(table, 'columnsAdvanced', items, false);

            items = [];
            $scope.dialog.namedCell.forEach(function(item) {
                var id = item['@id'];
                var record;
                if (id) {
                    // Try to read existing object.
                    record = rdf.filterSingle(rdf.findByUri(id));
                }
                if (!id || !record) {
                    // New object.
                    record = rdf.createObject('NamedCell');
                }
                // Now we have object to store data into.
                rdf.setString(record, 'name', item['name']);
                rdf.setInteger(record, 'rowNumber', item['rowNumber']);
                rdf.setInteger(record, 'columnNumber', item['columnNumber']);
                items.push(record);
            });
            rdf.updateObjects(table, 'namedCell', items, false);

            return rdf.getData();
        };

        $scope.onAddColumn = function () {
            $scope.dialog.columns.push({
                'name': '',
                'type': '',
                'lang': '',
                'typeFromDbf' : false,
                'uri': ''
            });
        };

        $scope.onAddAdvancedColumn = function () {
            $scope.dialog.advancedMapping.push({
                'uri': '',
                'template': ''
            });
        };

        $scope.onAddNamedCell = function () {
            $scope.dialog.namedCell.push({
                'name': '',
                'rowNumber': 0,
                'columnNumber': 0
            });
        };

        $scope.onDelete = function (data, index) {
            data.splice(index, 1);
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});
