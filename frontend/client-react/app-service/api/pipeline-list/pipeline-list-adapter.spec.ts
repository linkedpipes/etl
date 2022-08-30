import {jsonToJsonLdEntities} from "../../jsonld";
import {jsonLdToPipelineList} from "./pipeline-list-adapter";

test("Parse pipeline list.", () => {
  const json = [{
    "@graph": [{
      "@id": "http://etl.linkedpipes.com/resources/pipelines/1655048211160",
      "@type": ["http://linkedpipes.com/ontology/Pipeline"],
      "http://www.w3.org/2004/02/skos/core#prefLabel": [{
        "@value": "my-pipeline external"
      }]
    }],
    "@id": "http://etl.linkedpipes.com/resources/pipelines/1655048211160"
  }];
  const jsonld = jsonToJsonLdEntities(json);
  const actual = jsonLdToPipelineList(jsonld as any);
  const expected = {"data":[{
    "iri": "http://etl.linkedpipes.com/resources/pipelines/1655048211160",
    "label": "my-pipeline external",
    "tags": [],
  }]};
  expect(actual).toEqual(expected);
});
