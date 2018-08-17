((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    } else if (typeof module !== "undefined") {
        module.exports = definition();
    }
})(() => {

    function addSupportForSortByStr(repository, property, order) {
        const oldOnChange = repository["_onChange"];
        const onChange = (repository, changed) => {
            // Sort only when data change.
            if (changed === "data") {
                sortItemsStr(repository.data, property, order);
            }
            oldOnChange(repository, changed);
        };
        repository["_onChange"] = onChange;
    }

    function sortItemsStr(data, prop, order) {
        data.sort(
            (left, right) => left[prop].localeCompare(right[prop]) * order
        );
    }

    return {
        "sortedByStr": addSupportForSortByStr
    }

});