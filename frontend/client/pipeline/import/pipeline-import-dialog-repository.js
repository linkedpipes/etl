// TODO Merge repository with pipeline-list-repository ?
((definition) => {
    if (typeof define === "function" && define.amd) {
        define([
            "@client/app-service/vocabulary",
            "@client/app-service/repository/repository-infinite-scroll",
            "@client/app-service/jsonld/jsonld-source"
        ], definition);
    }
})((vocab, repositoryService, jsonLdSource) => {
    "use strict";

    const LP = vocab.LP;
    const SKOS = vocab.SKOS;

    const REPOSITORY_TEMPLATE = {
        "iri": {
            "$resource": null
        },
        "label": {
            "$property": SKOS.PREF_LABEL,
            "$type": "plain-string"
        }
    };

    function decorateItem(item) {
        if (item["label"] === undefined) {
            item["label"] = item["iri"];
        }
        item["searchLabel"] = item["label"].toLowerCase();
        item["filterLabel"] = true;
        item["selected"] = false;
    }


    function filter(item, filters, options) {
        if (filters.labelSearch === "") {
            item["filterLabel"] = true;
        } else if (item["iri"] === options) {
            item["filterLabel"] = true;
        } else {
            const query = filters.labelSearch.toLowerCase();
            item["filterLabel"] = item["searchLabel"].indexOf(query) !== -1;
        }
        return item["filterLabel"];
    }

    function increaseVisibleItemLimit(repository) {
        repositoryService.increaseVisibleItemsLimit(repository, 10);
    }

    function service($cookies) {

        function createRepository(filters) {
            const builder = jsonLdSource.createBuilder();
            builder.url("resources/pipelines");
            builder.itemType(LP.PIPELINE);
            builder.itemTemplate(REPOSITORY_TEMPLATE);
            return repositoryService.createWithInfiniteScroll({
                "itemSource": builder.build(),
                "newItemDecorator": decorateItem,
                "filter": (item, options) => filter(item, filters, options),
                "visibleItemLimit": getVisibleItemLimit(),
                "id": (item) => item["iri"]
            });
        }


        // TODO Move to "cookies" module.
        function getVisibleItemLimit() {
            const initialLimit = $cookies.get("lp-initial-list-size");
            if (initialLimit === undefined) {
                return 15;
            } else {
                return parseInt(initialLimit);
            }
        }

        return {
            "create": createRepository,
            "update": repositoryService.update,
            "onFilterChanged": repositoryService.onFilterChange,
            "load": repositoryService.initialFetch,
            "increaseVisibleItemLimit": increaseVisibleItemLimit
        };
    }

    service.$inject = ["$cookies"];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        app.service("pipeline.import.dialog.repository", service);
    }

});
