const repository = require("./repository-infinite-scroll");

test("Check properties of infinite scroll repository.", () => {
  const source = {};
  const repo = repository.createWithInfiniteScroll({
    "itemSource": source,
    "visibleItemLimit": 7,
    "id": item => item["id"]
  });
  expect(repo.visibleItems).toEqual([]);
  expect(repo.visibleItemLimit).toBe(7);
});

test("Test filter with infinity scroll.", () => {
  const source = {};
  const repo = repository.createWithInfiniteScroll({
    "itemSource": source,
    "visibleItemLimit": 1,
    "id": item => item["id"]
  });

  repo.data = [
    {"id": 0, "value": 0},
    {"id": 1, "value": 1},
    {"id": 2, "value": 2}
  ];

  repo["_filterFunction"] = () => true;
  repository.onFilterChange(repo);
  expect(repo.filteredItemCount).toBe(3);
  expect(repo.visibleItems.length).toBe(1);
  expect(repo.visibleItems[0].id).toEqual(2);

  repo["_filterFunction"] = (item) => item["value"] == 1;
  repository.onFilterChange(repo);
  expect(repo.filteredItemCount).toBe(1);
  expect(repo.visibleItems.length).toBe(1);
  expect(repo.visibleItems[0].id).toEqual(1);

  repo["_filterFunction"] = (item) => item["value"] >= 1;
  repository.onFilterChange(repo);
  expect(repo.filteredItemCount).toBe(2);
  expect(repo.visibleItems.length).toBe(1);
  expect(repo.visibleItems[0].id).toEqual(2);

  repository.increaseVisibleItemsLimit(repo, 2);
  expect(repo.visibleItems.length).toBe(2);
  expect(repo.visibleItems[0].id).toEqual(2);
  expect(repo.visibleItems[1].id).toEqual(1);

});

