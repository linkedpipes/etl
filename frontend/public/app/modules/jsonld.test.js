const jsonld = require("./jsonld");

test("Iterate resources.", () => {
    const data = [{}, {}, {}, {}];
    let counter = 0;
    jsonld.t.iterateResources(data, (resource) => {
        ++counter;
    });
    expect(counter).toBe(4);
});

test("Get resource by type.", () => {
    const resource = {"@type": "http://type"};
    const data = [{}, resource, {}];
    expect(jsonld.t.getResourceByType(data, "http://type")).toBe(resource);
});

test("Get resource by IRI.", () => {
    const resource = {"@id": "http://localhost/21"};
    const data = [{}, resource, {}];
    expect(jsonld.t.getResource(data, "http://localhost/21")).toBe(resource);
});

test("Get resources by references.", () => {
    const res1 = {"@id": "http://local/1"};
    const res2 = {"@id": "http://local/2"};
    const resRef = {
        "@id": "http://local/3",
        "http://local/ref": [
            {"@id": "http://local/1"},
            {"@id": "http://local/2"}
        ]
    };
    const data = [res1, resRef, res2];
    const actual = jsonld.t.getReferences(data, resRef, "http://local/ref");

    expect(actual).toEqual([res1, res2]);
});

test("Get graph.", () => {
    const graph = [{}, {}];
    const data = [{"@id": "http://graph", "@graph": graph}];
    expect(jsonld.q.getGraph(data, "http://graph")).toBe(graph);
});

test("Iterate graphs.", () => {
    const data = [
        {"@id": "http://graph/0", "@graph": []},
        {"@id": "http://graph/1", "@graph": []}
    ];
    let counter = 0;
    jsonld.q.iterateGraphs(data, (graph) => {
        ++counter;
    });
    expect(counter).toBe(2);
});

test("Iterate resources in graphs.", () => {
    const data = [
        {"@id": "http://graph/0", "@graph": [{}, {}]},
        {"@id": "http://graph/1", "@graph": [{},]}
    ];
    let counter = 0;
    jsonld.q.iterateResources(data, (resource, graph) => {
        ++counter;
    });
    expect(counter).toBe(3);
});
