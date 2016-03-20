define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'delimeter': '',
            'quote': '',
            'encoding': '',
            'header': true,
            'trim': true,
            'aboutUrl': '',
            'normalOutput': false,
            'fullMapping': false,
            'rowLimit': -1,
            'columns': [],
            'useBaseUri': false,
            'baseUri': '',
            'skipRows': -1
        };

        var prefix = {
            'csvw' : 'http://www.w3.org/ns/csvw#',
            'tabular': 'http://plugins.linkedpipes.com/ontology/t-tabular#'
        };

        var toType = function(record) {
            if (record['supressOutput']) {
                record['type'] = 'ignore';
            } else if(record['valueUrl']) {
                record['type'] = 'url';
            } else if (record['datatype'] === 'http://www.w3.org/2001/XMLSchema#string') {
                record['type'] = 'string';
            } else {
                record['type'] = 'typed';
            }
        };

        var fromType = function(record) {
            if (record['type'] === 'ignore') {
                record['supressOutput'] = true;
                record['valueUrl'] = '';
                record['datatype'] = '';
                record['lang'] = '';
            } else if (record['type'] === 'url') {
                record['supressOutput'] = false;
                record['datatype'] = '';
                record['lang'] = '';
            } else if (record['type'] === 'string') {
                record['supressOutput'] = false;
                record['valueUrl'] = '';
                record['datatype'] = 'http://www.w3.org/2001/XMLSchema#string';
            } else { //typed - unset valueUrl as it's used for detection.
                record['supressOutput'] = false;
                record['valueUrl'] = '';
                record['lang'] = '';
            }
        };

        var rdf = rdfService.create('');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var table = rdf.secureByType(prefix.csvw + 'Table');
            $scope.dialog.normalOutput = rdf.getBoolean(table, prefix.tabular + 'normalOutput');
            $scope.dialog.fullMapping = rdf.getBoolean(table, prefix.tabular + 'fullMapping');
            $scope.dialog.rowLimit = rdf.getInteger(table, prefix.tabular + 'rowLimit');
            $scope.dialog.useBaseUri = rdf.getBoolean(table, prefix.tabular + 'useBaseUri');
            $scope.dialog.baseUri = rdf.getString(table, prefix.tabular + 'baseUri');

            var dialect = rdf.secureObject(table, prefix.csvw + 'dialect', 'Dialect');
            $scope.dialog.delimeter = rdf.getString(dialect, prefix.csvw + 'delimeter');
            $scope.dialog.quote = rdf.getString(dialect, prefix.csvw + 'quoteChar');
            $scope.dialog.encoding = rdf.getString(dialect, prefix.csvw + 'encoding');
            $scope.dialog.header = rdf.getBoolean(dialect, prefix.csvw + 'header');
            $scope.dialog.trim = rdf.getBoolean(dialect, prefix.csvw + 'trim');
            $scope.dialog.skipRows = rdf.getInteger(dialect, prefix.csvw + 'skipRows');

            var schema = rdf.secureObject(table, prefix.csvw + 'tableSchema', 'Schema');
            $scope.dialog.aboutUrl = rdf.getString(schema, prefix.csvw + 'aboutUrl');

            $scope.dialog.columns = [];
            var columns = rdf.getObjects(schema, prefix.csvw + 'column');

            columns.forEach(function(item) {
                var columnRecord = {
                    '@id' : item['@id'],
                    'name': rdf.getString(item, prefix.csvw + 'name'),
                    'supressOutput': rdf.getBoolean(item, prefix.csvw + 'supressOutput'),
                    'required': rdf.getBoolean(item, prefix.csvw + 'required'),
                    'propertyUrl': rdf.getString(item, prefix.csvw + 'propertyUrl'),
                    'aboutUrl': rdf.getString(item, prefix.csvw + 'aboutUrl'),
                    'lang' : rdf.getString(item, prefix.csvw + 'lang'),
                    'datatype': rdf.getString(item, prefix.csvw + 'datatype'),
                    'valueUrl': rdf.getString(item, prefix.csvw + 'valueUrl')
                };
                toType(columnRecord);
                $scope.dialog.columns.push(columnRecord);
            });
        };

        $scope.getConfiguration = function () {
            var table = rdf.secureByType(prefix.csvw + 'Table');

            rdf.setBoolean(table, prefix.tabular + 'normalOutput', $scope.dialog.normalOutput);
            rdf.setBoolean(table, prefix.tabular + 'fullMapping', $scope.dialog.fullMapping);
            rdf.setInteger(table, prefix.tabular + 'rowLimit', $scope.dialog.rowLimit);
            rdf.setBoolean(table, prefix.tabular + 'useBaseUri', $scope.dialog.useBaseUri);
            rdf.setString(table, prefix.tabular + 'baseUri', $scope.dialog.baseUri);

            var dialect = rdf.secureObject(table, prefix.csvw + 'dialect', 'Dialect');
            rdf.setString(dialect, prefix.csvw + 'delimeter', $scope.dialog.delimeter);
            rdf.setString(dialect, prefix.csvw + 'quoteChar', $scope.dialog.quote);
            rdf.setString(dialect, prefix.csvw + 'encoding', $scope.dialog.encoding);
            rdf.setString(dialect, prefix.csvw + 'header', $scope.dialog.header);
            rdf.setBoolean(dialect, prefix.csvw + 'trim', $scope.dialog.trim);
            rdf.setInteger(dialect, prefix.csvw + 'skipRows', $scope.dialog.skipRows);

            var schema = rdf.secureObject(table, prefix.csvw + 'tableSchema', 'Schema');
            rdf.setString(schema, prefix.csvw + 'aboutUrl', $scope.dialog.aboutUrl);

            var columns = [];
            $scope.dialog.columns.forEach(function(item) {
                var id = item['@id'];
                var record;
                if (id) {
                    // Try to read existing object.
                    record = rdf.filterSingle(rdf.findByUri(id));
                }
                if (!id || !record) {
                    // New object.
                    record = rdf.createObject(prefix.csvw + 'Column');
                }
                // Unpack values in type to properties.
                fromType(item);
                // Now we have object to store data into.
                rdf.setString(record, prefix.csvw + 'name', item['name']);
                rdf.setBoolean(record, prefix.csvw + 'supressOutput', item['supressOutput']);
                rdf.setBoolean(record, prefix.csvw + 'required', item['required']);
                rdf.setString(record, prefix.csvw + 'propertyUrl', item['propertyUrl']);
                rdf.setString(record, prefix.csvw + 'aboutUrl', item['aboutUrl']);
                rdf.setString(record, prefix.csvw + 'lang', item['lang']);
                rdf.setString(record, prefix.csvw + 'datatype', item['datatype']);
                rdf.setString(record, prefix.csvw + 'valueUrl', item['valueUrl']);
                columns.push(record);
            });
            // Do not add new object, as we have already add them.
            rdf.updateObjects(schema, prefix.csvw + 'column', columns, false);

            return rdf.getData();
        };

        $scope.onAdd = function () {
            $scope.dialog.columns.push({
                'name': '',
                'type': 'typed'
            });
        };

        $scope.onDelete = function (index) {
            $scope.dialog.columns.splice(index, 1);
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});
