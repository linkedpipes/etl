/**
 * Define repository that can be used to show list of entries.
 *
 * As there might be duplicities in data (ie. empty item placeholder)
 * it must be used with "track by $index" suffix for ng-repeat.
 *
 * We can also add optimization of digest cycle (component site):
 * https://coderwall.com/p/d_aisq/speeding-up-angularjs-s-digest-loop
 *
 * In order to use updates the template must contains 'id' property
 * unique for each item.
 *
 */
((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    } else if (typeof module !== "undefined") {
        module.exports = definition();
    }
})(() => {
    "use strict";

    const HIDDEN_ELEMENT = {
        "isVisible": false
    };

    function createRepository(config) {
        return {
            // True once any data are loaded.
            "areDataReady": false,
            // True if there are no data in the data source.
            "isEmpty": true,
            // All the data retrieved from the source.
            "data": [],
            // Number of element that pass the filters.
            "filteredItemCount": 0,
            // Reference to item source. It must implement:
            // * fetch
            // * delete
            "_itemSource": config.itemSource,
            // Callback called for every new item.
            "_onNewItem": config.onNewItem,
            // Callback called before item is added to the repository.
            "_itemDecorator": config.newItemDecorator,
            // True if repository minimal update ie. not by full data query.
            "_supportMinimalUpdate": false,
            // Used to add additional functionality to repository
            // like infinity scroll.
            "_onChange": undefined,
            // Function used to filter items.
            "_filter": config.filter
        };
    }

    /**
     * It will also reverse the ordering of elements.
     */
    function createRepositoryWithInfiniteScroll(config) {
        const repository = createRepository(config);
        addSupportForInfiniteScroll(repository, config);
        return repository;
    }

    function addSupportForInfiniteScroll(repository, config) {
        // List of items that should be visible to the user,
        // can contains duplicity items.
        repository["visibleItems"] = [];
        // Limit size of visibleItem - do NOT change directly!
        repository["visibleItemLimit"] = config.visibleItemLimit;
        repository["_onChange"] = updateVisibleItemList;
    }

    /**
     * Given repository with data update restricted item list used for
     * infinite scroll.
     */
    function updateVisibleItemList(repository) {
        const initialVisibleItemsSize = repository.visibleItems.length;
        const visibleItems = repository.visibleItems;
        visibleItems.splice(0);
        for (let index = repository.data.length - 1; index >= 0; --index) {
            const item = repository.data[index];
            if (!item.isVisible) {
                continue;
            }
            if (visibleItems.length < repository.visibleItemLimit) {
                visibleItems.push(item);
            }
        }
        // Fill to the original size of repository.visibleItems
        // to re-use elements.
        let indexEnd = Math.min(
            repository.visibleItemLimit,
            initialVisibleItemsSize);
        for (let index = visibleItems.length; index < indexEnd; ++index) {
            visibleItems.push(HIDDEN_ELEMENT);
        }
    }

    function fetchItems(repository) {
        if (repository.areDataReady) {
            return new Promise.resolve();
        }
        onLoadingStarted(repository);
        return repository._itemSource.fetch().then((response) => {
            setRepositoryData(repository, response.data);
            onLoadingFinished(repository);
        }).catch((error) => {
            console.log("Repository loading failed.", error);
            onLoadingFailed(repository);
            throw error;
        });
    }

    function onLoadingStarted(repository) {
        repository.areDataReady = false;
    }

    function setRepositoryData(repository, data) {
        reportNewItems(repository, data);
        decorateItems(repository, data);
        filterItems(repository, data);
        repository.data = data;
        repository.isEmpty = data.length === 0;
        callOnChange(repository);
    }

    function reportNewItems(repository, data) {
        if (repository._onNewItem === undefined) {
            return;
        }
        data.forEach(repository._onNewItem);
    }

    function decorateItems(repository, data) {
        if (repository._itemDecorator === undefined) {
            return;
        }
        data.forEach(repository._itemDecorator);
    }

    function callOnChange(repository) {
        if (repository._onChange === undefined) {
            return;
        }
        repository._onChange(repository);
    }

    function onLoadingFinished(repository) {
        repository.areDataReady = true;
    }

    function onLoadingFailed(repository) {
        repository.areDataReady = false;
    }

    /**
     * @param userData Is given to to filter function as additional argument.
     */
    function onFilterChange(repository, userData) {
        filterItems(repository, repository.data, userData);
        callOnChange(repository);
    }

    function filterItems(repository, items, userData) {
        repository.filteredItemCount = 0;
        for (let index = 0; index < items.length; ++index) {
            const item = items[index];
            item["isVisible"] = repository._filter(item, userData);
            repository.filteredItemCount += item["isVisible"];
        }
        callOnChange(repository);
    }

    function updateItems(repository) {
        if (!repository.areDataReady) {
            return fetchItems(repository);
        }
        if (repository._supportMinimalUpdate) {
            // TODO Add support for minimal update using metadata.
        } else {
            return fullUpdate(repository);
        }
    }

    function fullUpdate(repository) {
        return repository._itemSource.fetch().then((response) => {
            mergeItemsToRepository(repository, response.data);
            deleteItemsFromRepository(repository, response.tombstones);
            callOnChange(repository);
        }).catch((error) => {
            console.log("Repository updating failed.", error);
            throw error;
        });
    }

    function mergeItemsToRepository(repository, newData) {
        const newItems = [];
        newData.forEach((item) => {
            const index = getIndexOfItem(repository, item.id);
            if (index === undefined) {
                newItems.push(item);
                repository.data.push(item);
            } else {
                repository.data[index] = item;
            }
        });
        const wasItemsNotDeleted = newItems.length === 0 &&
                repository.data.length === newData.length;
        if (!wasItemsNotDeleted) {
            removeMissingItems(repository.data, newData);
        }
        reportNewItems(repository, newItems);
        decorateItems(repository, newData);
        filterItems(repository, newData);
    }

    function removeMissingItems(data, referenceData) {
        const referenceIds = new Set();
        referenceData.forEach(item => referenceIds.add(item["id"]));
        for (let index = data.length - 1; index >= 0; --index) {
            if (referenceIds.has(data[index].id)) {
                continue;
            }
            data.splice(index, 1);
        }
    }

    function getIndexOfItem(repository, id) {
        for (let index = 0; index < repository.data.length; index++) {
            if (repository.data[index].id === id) {
                return index;
            }
        }
        return undefined;
    }

    function deleteItemsFromRepository(repository, tombstones) {
        if (tombstones === undefined) {
            return;
        }
        tombstones.forEach((id) => {
            const index = getIndexOfItem(repository, id);
            if (index === undefined) {
                return;
            }
            repository.data.splice(index, 1);
        });
    }

    function deleteItem(repository, id) {
        return repository._itemSource.delete(id).catch((error) => {
            console.log("Repository delete request failed.", error);
            throw error;
        })
    }

    function increaseVisibleItemsLimit(repository, increase) {
        if (repository.visibleItemLimit > repository.data.length) {
            return;
        }
        repository.visibleItemLimit = Math.min(
            repository.visibleItemLimit + increase,
            repository.filteredItemCount);
        callOnChange(repository);
    }

    function createConfigBuilder() {
        const c = {};
        // Defaults;
        c["filter"] = () => true;
        c["onNewItem"] = () => {};
        c["newItemDecorator"] = () => {};
        //
        return {
            "itemSource": (obj) => c["itemSource"] = obj,
            "onNewItem": (callback) => c["onNewItem"] = callback,
            "newItemDecorator": (callback) => c["newItemDecorator"] = callback,
            "visibleItemLimit": (number) => c["visibleItemLimit"] = number,
            "filter": (callback) => c["filter"] = callback,
            "build": () => c
        }
    }

    return {
        "create": createRepository,
        "createWithInfiniteScroll": createRepositoryWithInfiniteScroll,
        "fetch": fetchItems,
        "onFilterChange": onFilterChange,
        "update": updateItems,
        "delete": deleteItem,
        "increaseVisibleItemsLimit": increaseVisibleItemsLimit,
        "createConfigBuilder": createConfigBuilder
    }

});
