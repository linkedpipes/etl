const jsonldToJson = require("./jsonld-to-json");

test("Convert JSON-LD to JSON.", () => {
    const data = [
        {
            "@id": "http://graph/1",
            "@graph": [
                {
                    "@id": "http://res/1",
                    "@type": "http://type",
                    "http://label": "Label 1",
                    "http://tag": ["tag 1", "tag 2"]
                }
            ]
        },
        {
            "@id": "http://graph/2",
            "@graph": [
                {
                    "@id": "http://res/2",
                    "@type": "http://type",
                    "http://label": "Label 2",
                    "http://tag": ["tag 3", "tag 4"]
                }
            ]
        }
    ];
    const dataType = "http://type";
    const template = {
        "iri": {
            "$resource": ""
        },
        "label": {
            "$property": "http://label",
            "$type": "plain-string"
        },
        "tags": {
            "$property": "http://tag",
            "$type": "plain-array"
        }
    };
    const expected = [
        {"iri": "http://res/1", "label": "Label 1", "tags": ["tag 1", "tag 2"]},
        {"iri": "http://res/2", "label": "Label 2", "tags": ["tag 3", "tag 4"]}
    ];

    expect(jsonldToJson(data, dataType, template)).toEqual(expected);
});

test("Convert JSON-LD with @value in array to JSON.", () => {
    const data = [
        {
            "@id": "http://graph/1",
            "@graph": [
                {
                    "@id": "http://res/1",
                    "@type": "http://type",
                    "http://label": "Label 1",
                    "http://tag": [
                        {"@value": "tag 1"},
                        {"@value": "tag 2"}
                    ]
                }
            ]
        },
        {
            "@id": "http://graph/2",
            "@graph": [
                {
                    "@id": "http://res/2",
                    "@type": "http://type",
                    "http://label": "Label 2",
                    "http://tag": [
                        {"@value": "tag 3"},
                        {"@value": "tag 4"}
                    ]
                }
            ]
        }
    ];
    const dataType = "http://type";
    const template = {
        "iri": {
            "$resource": ""
        },
        "label": {
            "$property": "http://label",
            "$type": "plain-string"
        },
        "tags": {
            "$property": "http://tag",
            "$type": "plain-array"
        }
    };
    const expected = [
        {"iri": "http://res/1", "label": "Label 1", "tags": ["tag 1", "tag 2"]},
        {"iri": "http://res/2", "label": "Label 2", "tags": ["tag 3", "tag 4"]}
    ];

    expect(jsonldToJson(data, dataType, template)).toEqual(expected);
});