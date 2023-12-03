import {DebugMetadataList} from "./execution-debug-model";
import {jsonToDebugMetadataList} from "./execution-debug-adapter";
import {fetchContent, FetchFunction, fetchJson} from "../fetch-service";

export async function fetchDebugMetadata(
  fetchFunction: FetchFunction,
  execution: string, path: string, source: string | undefined,
  url: string
): Promise<DebugMetadataList> {
  const response = await fetchJson(fetchFunction, url);
  return jsonToDebugMetadataList(execution, path, source, response);
}

export async function fetchDebugData(
  fetchFunction: FetchFunction,
  url: string
): Promise<string> {
  return await fetchContent(fetchFunction, url);
}
