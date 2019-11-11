/**
 * Add support for infinite scroll to repository.
 *
 * As there might be duplicities in data (ie. empty item placeholder)
 * it must be used with "track by $index" suffix for ng-repeat.
 */
((definition) => {
  if (typeof define === "function" && define.amd) {
    define(["./repository"], definition);
  } else if (typeof module !== "undefined") {
    const repository = require("./repository");
    module.exports = definition(repository);
  }
})((repositoryModule) => {

  /**
   * Represent hidden element for repository with infinite scroll.
   */
  const HIDDEN_ELEMENT = {
    "isVisible": false
  };

  /**
   * It will also reverse the ordering of elements.
   */
  function createRepositoryWithInfiniteScroll(config) {
    const repository = repositoryModule.create(config);
    addSupportForInfiniteScroll(repository, config);
    return repository;
  }

  function addSupportForInfiniteScroll(repository, config) {
    // List of items that should be visible to the user,
    // can contains duplicity items.
    repository["visibleItems"] = [];
    // Limit size of visibleItem - do NOT change directly!
    repository["visibleItemLimit"] = config.visibleItemLimit;

    // Save old value and call it after us.
    const oldOnChange = repository["_onChange"];
    const onChange = (repository, changed) => {
      updateVisibleItemList(repository);
      oldOnChange(repository, changed);
    };
    repository["_onChange"] = onChange;
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

  function increaseVisibleItemsLimit(repository, increase) {
    if (repository.visibleItemLimit >= repository.data.length) {
      return;
    }
    repository.visibleItemLimit = Math.min(
      repository.visibleItemLimit + increase,
      repository.filteredItemCount);
    callOnChange(repository);
  }

  function callOnChange(repository) {
    repository._onChange(repository);
  }

  return Object.assign(repositoryModule, {
    "createWithInfiniteScroll": createRepositoryWithInfiniteScroll,
    "increaseVisibleItemsLimit": increaseVisibleItemsLimit
  });

});