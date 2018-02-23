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
    const PIPELINE_EDIT_URL = "#/pipelines/edit/canvas?pipeline=";

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
        },
        "tags": {
            "$property": LP.HAS_TAG,
            "$type": "plain-array"
        }
    };

    function decorateItem(item) {
        item["onClickUrl"] = PIPELINE_EDIT_URL + encodeURIComponent(item.iri);
        if (item["label"] === undefined) {
            item["label"] = item["iri"];
        }
        item["searchLabel"] = item["label"].toLowerCase();
        item["filterLabel"] = true;
        item["filterTags"] = true;
    }

    function addTagsToTagList(item, allTagList) {
        item["tags"].forEach((tag) => {
            var tagIndex = allTagList.indexOf(tag);
            if (tagIndex === -1) {
                allTagList.push(tag);
            }
        });
    }

    function filter(item, filters, options) {
        if (options === "chips") {
            filterTags(item, filters.tagsSearch);
        } else if (options === "label") {
            filterSearchLabel(item, filters.labelSearch);
        } else {
            filterTags(item, filters.tagsSearch);
            filterSearchLabel(item, filters.labelSearch);
        }
        return item["filterLabel"] && item["filterTags"];
    }

    function filterSearchLabel(item, value) {
        if (value === "") {
            item["filterLabel"] = true;
            return;
        }
        const query = value.toLowerCase();
        item["filterLabel"] = item["searchLabel"].indexOf(query) !== -1;
    }

    function filterTags(item, tags) {
        if (tags.length === 0) {
            item["filterTags"] = true;
            return;
        }
        if (item.tags.length < tags.length) {
        }
        for (let index = 0; index < tags.length; ++index) {
            const tag = tags[index];
            if (item.tags.indexOf(tag) === -1) {
                item["filterTags"] = false;
                return;
            }
        }
        item["filterTags"] = true;
    }

    function deletePipeline(pipeline, repository) {
        repositoryService.delete(repository, pipeline.id)
        .then(() => repositoryService.update(repository));
    }

    function increaseVisibleItemLimit(repository) {
        repositoryService.increaseVisibleItemsLimit(repository, 10);
    }

    function service($cookies, $http) {

        function createRepository(filters) {
            const builder = jsonLdRepositoryService.createConfigBuilder();
            builder.newItemDecorator(decorateItem);
            builder.onNewItem((item) => addTagsToTagList(item, filters.tagsAll));
            builder.visibleItemLimit(getVisibleItemLimit());
            builder.url("/resources/pipelines");
            builder.dataType(LP.PIPELINE);
            builder.tombstoneType(LP.TOMBSTONE);
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
            "delete": deletePipeline,
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
        app.service("pipeline.list.repository", service);
    }

});
