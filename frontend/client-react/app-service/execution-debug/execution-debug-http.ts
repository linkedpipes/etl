import {DebugMetadataList} from "./execution-debug-model";
import {jsonToDebugMetadataList} from "./execution-debug-adapter";
import {fetchContent, FetchFunction, fetchJson} from "../fetch-service";

export async function fetchDebugMetadata(
  fetchFunction: FetchFunction,
  execution: string, path: string, source: string | undefined,
  offset: number, limit: number
): Promise<DebugMetadataList> {
  let url = createMetadataUrl(execution, path);
  url += "?offset=" + offset + "&limit=" + limit;
  if (source !== undefined) {
    url += "&source=" + source;
  }
  const response = await fetchJson(fetchFunction, url);
  return jsonToDebugMetadataList(execution, path, source, response);
}

function createMetadataUrl(execution: string, path: string): string {
  if (path.length > 0 && path[0] !== "/") {
    path = "/" + path
  }
  return "./api/v1/debug/metadata/" + execution + path;
}

export async function fetchDebugData(
  fetchFunction: FetchFunction,
  url: string
): Promise<string> {
  return await fetchContent(fetchFunction, url);
}
