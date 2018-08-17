((definition) => {
    if (typeof define === "function" && define.amd) {
        define([
            "vocabulary",
            "app/modules/repository-infinite-scroll",
            "app/modules/repository-sorted",
            "app/modules/jsonld-source",
            "app/components/personalization/personalization"
        ], definition);
    }
})((vocab, repositoryService, repositorySorted, jsonLdSource,
    _personalization) => {
    "use strict";

    const LP = vocab.LP;
    const SKOS = vocab.SKOS;
    const PIPELINE_EDIT_URL = "#/pipelines/edit/canvas?pipeline=";

    const REPOSITORY_TEMPLATE = {
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
            const tagIndex = allTagList.indexOf(tag);
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

    function deletePipeline(repository, pipeline) {
        return repositoryService.deleteItem(repository, pipeline)
            .then(() => repositoryService.update(repository));
    }

    function increaseVisibleItemLimit(repository) {
        repositoryService.increaseVisibleItemsLimit(repository, 10);
    }

    function service($personalization) {

        function createRepository(filters) {
            const builder = jsonLdSource.createBuilder();
            builder.url("/resources/pipelines");
            builder.itemType(LP.PIPELINE);
            builder.tombstoneType(LP.TOMBSTONE);
            builder.itemTemplate(REPOSITORY_TEMPLATE);
            const repository = repositoryService.createWithInfiniteScroll({
                "itemSource": builder.build(),
                "onNewItem": (item) => addTagsToTagList(item, filters.tagsAll),
                "newItemDecorator": decorateItem,
                "filter": (item, options) => filter(item, filters, options),
                "visibleItemLimit": $personalization.getListSize(),
                "id": (item) => item["iri"]
            });
            addSorting(repository);
            return repository;
        }

        function addSorting(repository) {
            const orderBy = $personalization.getPipelineListOrder();
            if (orderBy === "title asc") {
                repositorySorted.sortedByStr(repository, "label", -1);
            } else if (orderBy === "title desc") {
                repositorySorted.sortedByStr(repository, "label", 1);
            } else {
                // Leave default.
            }
        }

        return {
            "create": createRepository,
            "update": repositoryService.update,
            "delete": deletePipeline,
            "onFilterChanged": repositoryService.onFilterChange,
            "load": repositoryService.initialFetch,
            "increaseVisibleItemLimit": increaseVisibleItemLimit
        };
    }

    service.$inject = ["personalization"];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        _personalization(app);
        app.service("pipeline.list.repository", service);
    }

});
