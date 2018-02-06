/**
 * Add support for JsonLd data to repository.
 */
((definition) => {
    if (typeof define === "function" && define.amd) {
        define(["./jsonld-to-json", "./repository"], definition);
    } else if (typeof module !== "undefined") {
        const jsonLdToJson = require("./jsonld-to-json");
        const repository = require("./repository");
        module.exports = definition(jsonLdToJson, repository);
    }
})((jsonLdToJson, repository) => {
    "use strict";

    const TOMBSTONE_TEMPLATE = {
        "id": {
            "$resource": null
        }
    };

    function createJsonLdRepositoryWithInfiniteScroll(config) {
        config.itemSource = createJsonLdSource(config);
        return repository.createWithInfiniteScroll(config);
    }

    function createJsonLdSource(config) {
        const url = config.url;
        const dataType = config.dataType;
        const tombstoneType = config.tombstoneType;
        const itemTemplate = config.itemTemplate;
        const $http = config.$http;
        return {
            "fetch": () =>
                fetchItems(url, dataType, tombstoneType, itemTemplate, $http),
            "delete": (id) =>
                deleteItem(id, $http)
        };
    }

    function fetchItems(url, dataType, tombstoneType, itemTemplate, $http) {
        return $http.get(url).then((response) => {
            const payload = response.data;
            const data = jsonLdToJson(
                payload, dataType, itemTemplate);
            const tombstones = jsonLdToJson(
                payload, tombstoneType, TOMBSTONE_TEMPLATE);
            return {
                "data": data,
                "tombstones": tombstonesToIds(tombstones)
            }
        })
    }

    function tombstonesToIds(tombstones) {
        return tombstones.map(item => item.id);
    }

    function deleteItem(id, $http) {
        const requestUrl = id;
        return $http({"method": "DELETE", "url": requestUrl});
    }

    // function fetchAllData(repository) {
    //     if (repository.areDataReady) {
    //         return new Promise.resolve();
    //     }
    //     onLoadingStarted(repository);
    //     return $http.get(repository._url).then((response) => {
    //         // TODO Check response status.
    //         console.time("repository-service.fetchAllData");
    //         const payload = repository._payloadWrapper(response);
    //         let dataAsJson = jsonLdToJson(
    //             getPayloadData(payload),
    //             repository._dataType,
    //             repository._itemTemplate);
    //         setRepositoryData(repository, dataAsJson);
    //         onLoadingFinished(repository);
    //         console.timeEnd("repository-service.fetchAllData");
    //     }).catch((error) => {
    //         console.timeEnd("repository-service.fetchAllData");
    //         console.log("Repository loading failed.", error);
    //         onLoadingFailed(repository);
    //         throw error;
    //     });
    // }
    //
    // function onLoadingStarted(repository) {
    //     repository.areDataReady = true;
    // }
    //
    // function setRepositoryData(repository, data) {
    //     // Reverse ordering fo the data.
    //     // TODO Move this to backend (storage, executor-monitor).
    //     reverseArrayOrdering(data);
    //     reportNewItems(repository, data);
    //     decorateNewItems(repository, data);
    //     setAllToVisible(data);
    //     repository._data = data;
    //     repository.isEmpty = data.length === 0;
    //     updateVisibleItemList(repository)
    // }
    //
    // function updateVisibleItemList(repository) {
    //     const initialVisibleItemsSize = repository.visibleItems.length;
    //     const visibleItems = repository.visibleItems;
    //     visibleItems.splice(0);
    //     let filteredItemCount = 0;
    //     for (let index = 0; index < repository._data.length; ++index) {
    //         const item = repository._data[index];
    //         if (!item.isVisible) {
    //             continue;
    //         }
    //         if (filteredItemCount < repository.visibleItemLimit) {
    //             visibleItems.push(item);
    //         }
    //         ++filteredItemCount;
    //     }
    //     // Fill to the original size of repository.visibleItems
    //     // to re-use elements.
    //     let indexEnd = Math.min(
    //         repository.visibleItemLimit,
    //         initialVisibleItemsSize);
    //     for (let index = visibleItems.length; index < indexEnd; ++index) {
    //         visibleItems.push(HIDDEN_ELEMENT);
    //     }
    //     // repository.visibleItems = visibleItems;
    //     repository.filteredItemCount = filteredItemCount;
    // }
    //
    // function reverseArrayOrdering(data) {
    //     const indexLimit = Math.floor(data.length / 2);
    //     const lastItemIndex = data.length - 1;
    //     for (let index = 0; index <= indexLimit; ++index) {
    //         const switchValue = data[index];
    //         data[index] = data[lastItemIndex - index];
    //         data[data.length - index] = switchValue;
    //     }
    // }
    //
    // function reportNewItems(repository, data) {
    //     if (repository._onNewItem === undefined) {
    //         return;
    //     }
    //     data.forEach(repository._onNewItem);
    // }
    //
    // function decorateNewItems(repository, data) {
    //     if (repository._itemDecorator === undefined) {
    //         return;
    //     }
    //     data.forEach(repository._itemDecorator);
    // }
    //
    // function setAllToVisible(data) {
    //     data.forEach((item) => {
    //         item["isVisible"] = true;
    //     });
    // }
    //
    // function onLoadingFinished(repository) {
    //     repository.areDataReady = false;
    // }
    //
    // function onLoadingFailed(repository) {
    //     repository.areDataReady = false;
    // }
    //
    // function getPayloadData(payload) {
    //     return payload.data;
    // }
    //
    // function updateData(repository) {
    //     if (!repository.areDataReady) {
    //         fetchAllData(repository);
    //     }
    //     if (repository._supportMinimalUpdate) {
    //         // TODO
    //     } else {
    //         return fullUpdate(repository);
    //     }
    // }
    //
    // function fullUpdate(repository) {
    //     onUpdatingStarted(repository);
    //     const url = repository.uri;
    //     return $http.get(url).then((response) => {
    //         console.time("repository-service.fullUpdate");
    //         const payload = repository._payloadWrapper(response);
    //         let dataAsJson = jsonLdToJson(
    //             getPayloadData(payload),
    //             repository._dataType,
    //             repository._itemTemplate);
    //
    //         updateItems(repository, dataAsJson);
    //         deleteItems(repository, dataAsJson);
    //
    //         console.timeEnd("repository-service.fullUpdate");
    //     })
    //     .catch((error) => {
    //         console.timeEnd("repository-service.fullUpdate");
    //         console.log("Repository updating failed.", error);
    //         onUpdatingFinished(repository);
    //         throw error;
    //     });
    // }
    //
    // function onUpdatingStarted(repository) {
    //     // No operation here.
    // }
    //
    // function onUpdatingFinished(repository) {
    //     // No operation here.
    // }
    //
    // // function getUpdateQueryConfiguration(repository) {
    // //     return {
    // //         "params": {
    // //             "changedSince": repository.update.last
    // //         }
    // //     };
    // // }
    //
    // function updateItems(repository, newData) {
    //     const newItems = [];
    //
    //     newData.forEach((item) => {
    //         mergeItemIntoRepository(repository, item)
    //     });
    // }
    //
    // // function mergeItemIntoRepository(repository, item) {
    // //     const index = getIndexOfItem(repository, item.id);
    // //     if (index === undefined) {
    // //         repository.data.push(item);
    // //     } else {
    // //         repository.data[index] = item;
    // //     }
    // // }
    //
    // // function getIndexOfItem(repository, id) {
    // //     // TODO PERFORMANCE We can use hash map to keep indexes.
    // //     for (let index = 0; index < repository.data.length; index++) {
    // //         if (repository.data[index].id === id) {
    // //             return index;
    // //         }
    // //     }
    // //     return undefined;
    // // }
    //
    // // function deleteItems(repository, dataToDelete) {
    // //     if (dataToDelete === undefined) {
    // //         return;
    // //     }
    // //     dataToDelete.forEach((id) => {
    // //         const index = getIndexOfItem(repository, id);
    // //         if (index === undefined) {
    // //             return;
    // //         }
    // //         repository.data.splice(index, 1);
    // //     });
    // // }
    //
    // // function deleteData(repository, id) {
    // //     const iri = repository.uri + "/" + id;
    // //     return $http({"method": "DELETE", "url": iri})
    // //     .then(() => {
    // //         const index = getIndexOfItem(repository, id);
    // //         if (index === undefined) {
    // //             return;
    // //         }
    // //         if (!repository.removeOnDelete) {
    // //             repository.data.splice(index, 1);
    // //         } else {
    // //             decorateDeletedData(repository, repository.data[index]);
    // //         }
    // //     })
    // //     .catch((error) => {
    // //         console.log("Repository delete request failed.", error);
    // //         throw error;
    // //     })
    // // }
    // //
    // // function decorateDeletedData(repository, item) {
    // //     const decorator = repository.decorators.delete;
    // //     if (decorator === undefined || decorator === null) {
    // //         return;
    // //     }
    // //     decorator(item);
    // // }
    //
    // /**
    //  * Apply filter function on all items.
    //  */
    // function filterItems(repository, filter) {
    //     const data = repository._data;
    //     for (let index = 0; index < data.length; ++index) {
    //         const item = data[index];
    //         item["isVisible"] = filter(item);
    //     }
    //     updateVisibleItemList(repository);
    // }
    //
    // function increaseVisibleItemLimit(repository, increase) {
    //
    //     if (repository.visibleItemLimit > repository._data.length) {
    //         return;
    //     }
    //     repository.visibleItemLimit = Math.min(
    //         repository.visibleItemLimit + increase,
    //         repository.filteredItemCount);
    //     updateVisibleItemList(repository);
    // }
    //
    // let $http;
    //
    // function service(_$http) {
    //     $http = _$http;
    //     this.create = createJsonLdRepositoryWithInfiniteScroll;
    //     // this.fetch = fetchAllData;
    //     // this.update = updateData;
    //     // // this.delete = (repository, id) => deleteData(repository, id);
    //     // this.filter = filterItems;
    //     // this.increaseVisibleItemLimit = increaseVisibleItemLimit;
    // }
    //
    // service.$inject = ["$http"];
    //
    // let _initialized = false;
    // return function init(app) {
    //     if (_initialized) {
    //         return;
    //     }
    //     _initialized = true;
    //     app.service("services.repository", service);
    // };

    function createConfigurationBuilder() {
        const builder = repository.createConfigBuilder();
        const c = builder.build();
        builder["url"] = (str) => c["url"] = str;
        builder["dataType"] = (str) => c["dataType"] = str;
        builder["tombstoneType"] = (str) => c["tombstoneType"] = str;
        builder["itemTemplate"] = (obj) => c["itemTemplate"] = obj;
        builder["$http"] = ($http) => c["$http"] = $http;
        return builder;
    }

    return {
        "createWithInfiniteScroll": createJsonLdRepositoryWithInfiniteScroll,
        "createConfigBuilder": createConfigurationBuilder
    }
});
