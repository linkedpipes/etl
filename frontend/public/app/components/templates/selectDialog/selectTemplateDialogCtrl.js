define([], function () {
    function controler($scope, $mdDialog, templatesRepository, filter) {

        // {label: search.label}
        $scope.filterItems = function (item) {
            if (!$scope.search || $scope.search === '') {
                return true;
            }
            var searchString = $scope.search.toLowerCase();
            return item.component.filterString.indexOf(searchString) !== -1;
        };

        $scope.onSelect = function (item) {
            $mdDialog.hide(item);
        };

        $scope.onCancel = function () {
            $mdDialog.cancel();
        };

        /**
         * For given component return name name of the icon.
         */
        var returnIconName = function (component) {
            if (component.type === 'http://etl.linkedpipes.com/ontology/component/type/Extractor') {
                return 'file_download';
            } else if (component.type === 'http://etl.linkedpipes.com/ontology/component/type/Transformer') {
                return 'transform';
            } else if (component.type === 'http://etl.linkedpipes.com/ontology/component/type/Loader') {
                return 'file_upload';
            } else if (component.type === 'http://etl.linkedpipes.com/ontology/component/type/Executor') {
                return 'call_split';
            } else if (component.type === 'http://etl.linkedpipes.com/ontology/component/type/Quality') {
                return 'help_outline';
            } else {
                return '';
            }
        };

        var addAllTemplates = function () {
            var data = [];
            templatesRepository.getTemplates().forEach(function (component) {
                data.push({
                    'label': component['label'],
                    'icon': returnIconName(component),
                    'component': component,
                    'order': 0
                });
            });
            $scope.data = data;
        };

        (function initialize() {
            $scope.search = '';
            //
            if (filter.source) {
                //
                var sourceTemplate = templatesRepository.getTemplate(filter.source.templateIri);
                var sourcePort;
                for (var index in sourceTemplate.outputs) {
                    if (sourceTemplate.outputs[index]['binding'] === filter.source.binding) {
                        sourcePort = sourceTemplate.outputs[index];
                        break;
                    }
                }
                if (!sourcePort) {
                    console.log('Error missing sourcePort, filter: ', filter, ' tempalte: ', sourceTemplate);
                    addAllTemplates();
                    return;
                }
                // Add only those that pass the filter.
                var data = [];
                templatesRepository.getTemplates().forEach(function (component) {
                    if (component.inputs.length === 0) {
                        return;
                    }
                    //
                    var order = 0;
                    if (sourceTemplate.followup[component['id']]) {
                        order = sourceTemplate.followup[component['id']];
                    }
                    //
                    var newObjects = [];
                    component.inputs.forEach(function (port) {
                        if (port['type']['0'] === sourcePort['type']['0']) {
                            var record = {
                                'label': component['label'],
                                'icon': returnIconName(component),
                                'component': component,
                                'portBinding': port['binding'],
                                'portLabel': port['label'],
                                'order': order
                            };
                            data.push(record);
                            newObjects.push(record);
                        }
                    });
                    // Hyde port label if there is only one instance.
                    if (newObjects.length === 1) {
                        newObjects[0].portLabel = '';
                    }
                });
                $scope.data = data;
            } else {
                addAllTemplates();
            }
        })();

    }
    controler.$inject = ['$scope', '$mdDialog', 'components.templates.services.repository', 'filter'];
    //
    function init(app) {
        app.controller('components.templates.select.dialog', controler);
    }
    //
    return init;
});

