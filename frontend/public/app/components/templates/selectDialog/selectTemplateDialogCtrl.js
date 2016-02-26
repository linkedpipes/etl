define([], function () {
    function controler($scope, $mdDialog, templatesRepository, filter) {

        $scope.onSelect = function (item) {
            $mdDialog.hide(item);
        };

        $scope.onCancel = function () {
            $mdDialog.cancel();
        };

        var addAllTemplates = function() {
            var data = [];
            templatesRepository.getTemplates().forEach(function (template) {
                data.push({
                    'label' : template['label'],
                    'component' : template
                });
            });
            $scope.data = data;
        };

        (function initialize() {
            if (filter.source) {
                //
                var sourceTemplate = templatesRepository.getTemplate(filter.source.templateUri);
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
                templatesRepository.getTemplates().forEach(function (template) {
                    // For now we require one port only!
                    if (template.inputs.length !== 1) {
                        return;
                    }
                    // Check for binding.
                    var port = template.inputs[0];
                    if (port['type']['0'] === sourcePort['type']['0']) {
                        data.push({
                            'label' : template['label'],
                            'component' : template,
                            'portBinding' : port['binding']
                        });
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

