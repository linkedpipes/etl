define([], function () {
    function factoryFunction($http) {
        var service = {};

        var templates = {
            'list': [],
            'map': {}
        };

        /**
         * Load templates, if templates are already loaded imiidiately call onSucess.
         *
         * @param onSucess Called on sucess.
         * @param onFailure Called on failure.
         */
        service.load = function (onSucess, onFailure) {
            console.log('templatesRepository.load');
            // TODO: Optimize and load only if necesarry - or use an update function?
            $http.get("./resources/components").then(function (response) {
                // We must not change the array entity as that would,
                // break synchronization - also we only add new templates.
                response.data['payload'].forEach(function (item) {
                    if (!templates.map[item['id']]) {
                        // Construct filtering string - the value is used as a filter
                        // for searching DPUs.
                        item['filterString'] = item.label.toLowerCase();
                        item['keyword'].forEach(function(word) {
                            item['filterString'] += ',' + word.toLowerCase();
                        });
                        //
                        templates.map[item['id']] = item;
                        templates.list.push(item);
                    }
                });
                if (onSucess) {
                    onSucess();
                }
            }, function(response) {
                if (onFailure) {
                    onFailure(response);
                }
            });
        };

        /**
         *
         * @returns List of templates, can be used for binding as the reference will not change.
         */
        service.getTemplates = function () {
            return templates.list;
        };

        /**
         *
         * @param uri Template URI.
         * @returns Template object or nothing if URI is invalid.
         */
        service.getTemplate = function (uri) {
            return templates.map[uri];
        };

        /**
         * Make sure that given template has configuration loaded.
         *
         * @param template
         * @param onSucess Called on sucess.
         * @param onFailure Called on failure, parameter is a failure message from $http.get.
         */
        service.fetchTemplateConfiguration = function (template, onSucess, onFailure) {
            $http.get(template['configurationUri'], {
                // Supress default AngularJS conversion form JSON to JavaScript object.
                transformResponse: function (data, headersGetter) {
                    return data;
                }
            }).then(function (response) {
                template['configuration'] = JSON.parse(response.data);
                if (onSucess) {
                    onSucess();
                };
            }, function(response) {
                if (onFailure) {
                    onFailure(response);
                }
            });
        };

        return service;
    }
    factoryFunction.$inject = ['$http'];
    /**
     *
     * @param app Angular modeule.
     */
    function register(app) {
        app.factory('components.templates.services.repository', factoryFunction);
    }
    return register;
});