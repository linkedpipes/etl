/**
 * Define service that should be used for periodic update tasks. The service
 * reset on the page change.
 *
 * TODO: We can report to some component about failure or sucess of operation, we can also directly bind to
 *  refresher service.
 * TODO Remove and repalce with JSON-LD repository.
 *
 */
define([], function () {
    function factoryFunction($http, refreshService) {
        var service = {};

        /**
         * Create and return repository object.
         *
         * @parametr uri
         * @parametr updateOperation Called on changed and new items. Can be used to compute aditional statistics.
         */
        service.createRepository = function (configuration) {
            var repository = {
                'loaded': false,
                'loading': false,
                'updating': false,
                'offline': false,
                'update': {
                    'last': 0,
                    'operation': configuration.updateOperation
                },
                'delete': {
                    'operation': configuration.deleteOperation
                },
                'uri': configuration.uri,
                'data': []
            };
            return repository;
        };

        /**
         * Query all data for given repository.
         */
        service.get = function (repository, onSucess, onFailure) {
            repository.loading = true;
            $http.get(repository.uri).then(function (response) {
                repository.update.last = response.data.metadata.created;
                repository.data = response.data.payload;

                if (repository.update.operation) {
                    repository.data.forEach(function (item) {
                        repository.update.operation(item);
                    });
                }

                repository.loaded = true;
                repository.loading = false;
                if (onSucess) {
                    onSucess();
                }
            }, function (response) {
                console.log(response);
                // We will try to load data later.
                repository.loading = false;
                if (onFailure) {
                    onFailure(response);
                }
            });
        };

        /**
         * Update data for given repository.
         */
        service.update = function (repository, onSucess, onFailure) {
            if (!repository.loaded) {
                // Not loaded yet.
                if (repository.loading) {
                    // We are loading just now.
                    return;
                } else {
                    service.get(repository, onSucess, onFailure);
                }
            }
            if (repository.updating) {
                return;
            }
            repository.updating = true;

            $http.get(repository.uri, {
                params: {'changedSince': repository.update.last}
            }).then(function (response) {
                repository.update.last = response.data.metadata.created;
                response.data.payload.forEach(function (item) {
                    if (repository.update.operation) {
                        repository.update.operation(item);
                    }
                    // Check if we already have such item or not.
                    // TODO PERFORMANCE We can use hash map to keep indexes.
                    var isNew = true;
                    for (i = 0; i < repository.data.length; i++) {
                        if (repository.data[i].id === item.id) {
                            // Replace original value.
                            repository.data[i] = item;
                            isNew = false;
                            break;
                        }
                    }
                    if (isNew) {
                        repository.data.push(item);
                    }
                });
                // Remove items.
                if (response.data.deleted) {
                    response.data.deleted.forEach(function (id) {
                        for (var i = repository.data.length - 1; i >= 0; --i) {
                            if (repository.data[i].id === id) {
                                repository.data.splice(i, 1);
                            }
                        }
                    });
                }
                if (onSucess) {
                    onSucess();
                }
                repository.updating = false;
            }, function (response) {
                repository.updating = false;
                if (onFailure) {
                    onFailure();
                }
            });
        };

        service.delete = function (repository, id, onSucess, onFailure) {
            var uri = repository.uri + '/' + id;
            $http({method: 'DELETE', url: uri}).then(function (response) {
                // The item is removed with next synchronization, for now just mark it as deleted.
                for (var i = repository.data.length - 1; i >= 0; --i) {
                    if (repository.data[i].id === id) {
                        if (repository.delete.operation) {
                            repository.delete.operation(repository.data[i]);
                        }
                        break;
                    }
                }
                if (onSucess) {
                    onSucess();
                }
            }, function (response) {
                if (onFailure) {
                    onFailure(response);
                }
            });
        };

        return service;
    }
    //
    factoryFunction.$inject = ['$http', 'service.refresh'];
    //
    function init(app) {
        app.factory('services.repository', factoryFunction);
    }
    return init;
});