const repository = require("./repository");

test("Check repository public properties.", () => {
    const source = {};
    const repo = repository.create({
        "itemSource": source,
        "id": item => item["id"]
    });
    expect(repo.data).toEqual([]);
    expect(repo.areDataReady).toBe(false);
    expect(repo.isEmpty).toBe(true);
    expect(repo.filteredItemCount).toBe(0);
});

test("Minimal update full use-case.", () => {
    const fetchDataResponse = {
        // Initial data.
        0: {
            "data": [
                {"id": 2, "shouldPassFilter": true},
                {"id": 0, "shouldPassFilter": true}
            ],
            "tombstones": [],
            "timeStamp": 1

        },
        // One new item, preserve missing item.
        1: {
            "data": [
                {"id": 1, "shouldPassFilter": true},
                {"id": 0, "shouldPassFilter": false}
            ],
            "tombstones": [],
            "timeStamp": 2
        },
        // Preserve all items, remove one by tombstone.
        2: {
            "data": [],
            "tombstones": [1],
            "timeStamp": 3
        },
        // No change.
        3: {
            "data": [],
            "tombstones": [],
            "timeStamp": 4
        }
    };

    let deleteCallCounter = 0;
    const source = {
        "fetch": (changedSince) => {
            if (changedSince === undefined) {
                return Promise.resolve(fetchDataResponse[0]);
            } else {
                return Promise.resolve(fetchDataResponse[changedSince]);
            }
        },
        "deleteById": (item) => {
            deleteCallCounter += 1;
            return Promise.resolve();
        },
        "incrementalUpdateSupport": true
    };

    let newItemCounter = 0;
    let decoratorCounter = 0;
    const repo = repository.create({
        "itemSource": source,
        "onNewItem": (item) => {
            newItemCounter += 1;
        },
        "newItemDecorator": (item) => {
            decoratorCounter += 1
        },
        "filter": (item) => item["shouldPassFilter"],
        // Define ordering, id is used by default.
        "order": (left, right) => left["id"] - right["id"],
        "id": item => item["id"]
    });

    return repository.initialFetch(repo)
        .then(() => {
            expect(repo.data.length).toBe(2);

            expect(repo.data[0]["id"]).toBe(0);
            expect(repo.data[0]["isVisible"]).toBe(true);

            expect(repo.data[1]["id"]).toBe(2);
            expect(repo.data[1]["isVisible"]).toBe(true);

            expect(repo.areDataReady).toBe(true);
            expect(repo.isEmpty).toBe(false);
            expect(newItemCounter).toBe(2);
            expect(decoratorCounter).toBe(2);
            return repository.update(repo);
        }).then(() => {
            expect(repo.data.length).toBe(3);

            expect(repo.data[0]["id"]).toBe(0);
            expect(repo.data[0]["isVisible"]).toBe(false);

            expect(repo.data[1]["id"]).toBe(1);
            expect(repo.data[1]["isVisible"]).toBe(true);

            expect(repo.data[2]["id"]).toBe(2);
            expect(repo.data[2]["isVisible"]).toBe(true);

            expect(repo.areDataReady).toBe(true);
            expect(repo.isEmpty).toBe(false);
            expect(newItemCounter).toBe(3);
            expect(decoratorCounter).toBe(4);

            return repository.deleteItem(repo, {"id": 1});
        }).then(() => {
            // Delete request does not delete the data, only issue a request.
            expect(repo.data.length).toBe(3);
            return repository.update(repo);
        }).then(() => {
            expect(repo.data.length).toBe(2);

            expect(repo.data[0]["id"]).toBe(0);
            expect(repo.data[0]["isVisible"]).toBe(false);

            expect(repo.data[1]["id"]).toBe(2);
            expect(repo.data[1]["isVisible"]).toBe(true);

            expect(repo.areDataReady).toBe(true);
            expect(repo.isEmpty).toBe(false);
            expect(newItemCounter).toBe(3);
            expect(decoratorCounter).toBe(4);

            return repository.update(repo);
        }).then(() => {
            expect(repo.data.length).toBe(2);

            expect(repo.data[0]["id"]).toBe(0);
            expect(repo.data[0]["isVisible"]).toBe(false);

            expect(repo.data[1]["id"]).toBe(2);
            expect(repo.data[1]["isVisible"]).toBe(true);

            expect(repo.areDataReady).toBe(true);
            expect(repo.isEmpty).toBe(false);
            expect(newItemCounter).toBe(3);
            expect(decoratorCounter).toBe(4);
        });
});

test("Fetch items, check they are loaded and decorated.", () => {
    const source = {
        "fetch": () => Promise.resolve({
            "data": [
                {"id": 0, "value": 0},
                {"id": 1, "value": 1},
                {"id": 2, "value": 2}
            ]
        })
    };
    let newItemCounter = 0;
    const repo = repository.create({
        "itemSource": source,
        "onNewItem": () => {
            ++newItemCounter
        },
        "newItemDecorator": (item) => item["decorated"] = true,
        "id": item => item["id"]
    });
    return repository.initialFetch(repo).then(() => {
        expect(repo.data.length).toBe(3);
        expect(repo.areDataReady).toBe(true);
        expect(repo.isEmpty).toBe(false);
        expect(newItemCounter).toBe(3);
        repo.data.forEach((item) => {
            expect(item.decorated).toBe(true);
        });
        expect(repo.data[0].id).toEqual(0);
        expect(repo.data[1].id).toEqual(1);
        expect(repo.data[2].id).toEqual(2);
    });
});


test("Repository ignores multiple calls of initialFetch.", () => {
    let callCount = 0;
    const source = {
        "fetch": () => {
            callCount += 1;
            return Promise.resolve({"data": []})
        }
    };
    const repo = repository.create({
        "itemSource": source,
        "id": item => item["id"]
    });
    return repository.initialFetch(repo).then(() => {
        return repository.initialFetch(repo);
    }).then(() => {
        expect(callCount).toBe(1);
    });
});

test("Check filter change.", () => {

    const source = {};
    const repo = repository.create({
        "itemSource" : source,
        "filter": (item) => item["value"] < 2,
        "id": item => item["id"]
    });

    // Used instead of calling fetch.
    repo.data = [
        {"id": 0, "value": 0},
        {"id": 1, "value": 1},
        {"id": 2, "value": 2}
    ];

    repository.onFilterChange(repo);
    expect(repo.filteredItemCount).toBe(2);
    expect(repo.data[0].isVisible).toEqual(true);
    expect(repo.data[1].isVisible).toEqual(true);
    expect(repo.data[2].isVisible).toEqual(false);

    // Update filter function directly in the repository.
    repo["_filterFunction"] = (item) => item["value"] === 1;
    repository.onFilterChange(repo);
    expect(repo.filteredItemCount).toBe(1);
    expect(repo.data[0].isVisible).toEqual(false);
    expect(repo.data[1].isVisible).toEqual(true);
    expect(repo.data[2].isVisible).toEqual(false);
});

test("After update, items can be removed because they are missing.", () => {
    const source = {
        "fetch": () => Promise.resolve({
            "data": [
                {"id": 0, "value": 10},
                {"id": 1, "value": 11},
                {"id": 2, "value": 12}
            ],
            "tombstones": []
        })
    };

    let newItemCounter = 0;

    const repo = repository.create({
        "itemSource": source,
        "onNewItem": () => {
            ++newItemCounter
        },
        "newItemDecorator": (item) => item["decorated"] = true,
        "id": item => item["id"]
    });

    repo.areDataReady = true;
    // Set some initial data.
    repo.data = [
        {"id": 0, "value": 0},
        {"id": 1, "value": 1},
        {"id": 3, "value": 1}
    ];

    expect(repo.data.length).toBe(3);

    return repository.update(repo).then(() => {
        expect(repo.data.length).toBe(3);
        expect(newItemCounter).toBe(1);

        repo.data.forEach((item) => {
            expect(item.decorated).toBe(true);
            expect(item.id + 10).toBe(item.value);
        });

        expect(repo.data[0].id).toEqual(0);
        expect(repo.data[1].id).toEqual(1);
        expect(repo.data[2].id).toEqual(2);
    });
});

test("Upon update item can be deleted when a tombstone is returned.", () => {
    const source = {
        "fetch": () => Promise.resolve({
            "data": [
                {"id": 1}
            ],
            "tombstones": [0]
        })
    };
    const repo = repository.create({
        "itemSource": source,
        "id": item => item["id"]
    });

    repo.areDataReady = true;
    repo.data = [
        {"id": 0},
        {"id": 1},
    ];

    return repository.update(repo).then(() => {
        expect(repo.data.length).toBe(1);
        expect(repo.data[0].id).toEqual(1);
    });
});

test("Update repository data with filters.", () => {
    const source = {
        "fetch": () => Promise.resolve({
            "data": [
                {"id": 0, "value": 0},
                {"id": 1, "value": 1},
                {"id": 2, "value": 0}
            ],
            "tombstones": []
        })
    };

    const repo = repository.create({
        "itemSource": source,
        "filter": (item) => item["value"] === 1,
        "id": item => item["id"]
    });

    repo.areDataReady = true;
    repo.data = [
        {"id": 0, "value": 1},
        {"id": 1, "value": 0}
    ];

    repository.onFilterChange(repo);
    expect(repo.data[0].isVisible).toEqual(true);
    expect(repo.data[1].isVisible).toEqual(false);

    return repository.update(repo).then(() => {
        expect(repo.data.length).toBe(3);
        expect(repo.data[0].isVisible).toEqual(false);
        expect(repo.data[1].isVisible).toEqual(true);
        expect(repo.data[2].isVisible).toEqual(false);
    });
});


test("Delete last item.", () => {
    const source = {
        "fetch": () => Promise.resolve({
            "data": [ ],
            "tombstones": [0]
        })
    };
    const repo = repository.create({
        "itemSource": source,
        "id": item => item["id"]
    });

    repo.areDataReady = true;
    repo.data = [
        {"id": 0},
    ];

    return repository.update(repo).then(() => {
        expect(repo.data.length).toBe(0);
        expect(repo.isEmpty).toEqual(true);
    });
});