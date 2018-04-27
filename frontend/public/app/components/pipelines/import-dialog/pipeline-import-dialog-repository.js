// TODO Merge repository with pipeline-list-repository ?
((definition) => {
    if (typeof define === "function" && define.amd) {
        define([
            "vocabulary",
            "app/modules/repository",
            "app/modules/jsonld-repository"
        ], definition);
    } else if (typeof module !== "undefined") {
        module.exports = definition();
    }
})((vocab, repositoryService, jsonLdRepositoryService) => {
    "use strict";

    const LP = vocab.LP;
    const SKOS = vocab.SKOS;

    // TODO Merge id and iri into one value.
    const REPOSITORY_TEMPLATE = {
        "id": {
            "$resource": null
        },
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
        const activePipeline = options;
        if (filters.labelSearch === "") {
            item["filterLabel"] = true;
        } else if (item["iri"] === activePipeline) {
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

    function service($cookies, $http) {

        function createRepository(filters) {
            const builder = jsonLdRepositoryService.createConfigBuilder();
            builder.newItemDecorator(decorateItem);
            builder.visibleItemLimit(getVisibleItemLimit());
            builder.url("/resources/pipelines");
            builder.dataType(LP.PIPELINE);
            builder.itemTemplate(REPOSITORY_TEMPLATE);
            builder.$http($http);
            builder.filter((item, options) => filter(item, filters, options));
            const config = builder.build();
            return jsonLdRepositoryService.createWithInfiniteScroll(config);
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
            "load": repositoryService.fetch,
            "increaseVisibleItemLimit": increaseVisibleItemLimit
        };
    }

    service.$inject = ["$cookies", "$http"];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        app.service("pipeline.import.dialog.repository", service);
    }

});
