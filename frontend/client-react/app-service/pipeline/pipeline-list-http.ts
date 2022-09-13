import {jsonToJsonLdEntities} from "../jsonld";
import {FetchFunction, fetchJson} from "../fetch-service";
import {PipelineList} from "./pipeline-list-model";
import {jsonLdToPipelineList} from "./pipeline-list-adapter";

export async function fetchPipelineList(
  fetchFunction: FetchFunction
): Promise<PipelineList> {
  const url = "./api/v1/pipelines-list";
  const json = fetchJson(fetchFunction, url);
  const entities = jsonToJsonLdEntities(json);
  return jsonLdToPipelineList(entities);
}
