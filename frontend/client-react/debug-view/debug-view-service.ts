import {useCallback, useEffect, useState} from "react";
import {
  DebugEntry,
  DebugFileEntry,
  DebugFileMetadata,
  DebugMetadata,
  DebugMetadataList,
  fetchDebugData,
  fetchDebugMetadata,
  isDebugFileEntry
} from "../app-service/execution-debug";
import {useFetch} from "../app-service/react";
import {FetchFunction} from "../app-service/fetch-service";
import {useSearchParams} from "react-router-dom";

const FILE_PREVIEW_LIMIT = 512 * 1024;

/**
 * Provide access to debug metadata for given parameters.
 */
export function useDebugMetadata(
  execution: string, path: string, source: string | undefined,
  offset: number, limit: number,
) {
  const fetchWrap = useCallback((fetchFunction: FetchFunction) => {
    return fetchDebugMetadata(
      fetchFunction, execution, path, source, offset, limit);
  }, [execution, path, source, offset, limit]);
  return useFetch<DebugMetadataList>(fetchWrap);
}

/**
 * For given entry return URL of page with detail.
 */
export function getMetadataUrl(entry: DebugEntry) {
  const execution = encodeURIComponent(entry.execution);
  const path = encodeURIComponent(entry.fullPath);
  let result = `/debug?execution=${execution}&path=${path}`;
  if (isDebugFileEntry(entry) && entry.source !== undefined) {
    result += "&source=" + encodeURIComponent(entry.source);
  }
  return result;
}

/**
 * For given entry return URL of page with detail. As there is no source
 * this is used to navigate to directories.
 */
export function getMetadataUrlForDirectory(execution: string, path?: string) {
  let result = "/debug?execution=" + encodeURIComponent(execution);
  if (path !== undefined) {
    result += "&path=" + encodeURIComponent(path);
  }
  return result;
}

export function useNavigateToEntry() {
  const [, setSearchParams] = useSearchParams();
  return (entry: DebugEntry) => {
    const params: any = {
      "execution": entry.execution,
      "path": entry.fullPath,
    };
    if (isDebugFileEntry(entry) && entry.source !== undefined) {
      params["source"] = entry.source;
    }
    setSearchParams(params);
  };
}

/**
 * Return function available to navigate to different pages.
 * @param entry
 */
export function useNavigateToPage(entry: DebugMetadata) {
  const [, setSearchParams] = useSearchParams();
  return (page: number) => {
    // There is no source for directories.
    setSearchParams({
      "execution": entry.execution,
      "path": entry.fullPath,
      "page": String(page),
    });
  };
}

/**
 * Return URL that can be used to download content of represented file.
 */
export function getDownloadUrl(entry: DebugFileMetadata | DebugFileEntry) : {
  url: string,
  name: string
} {
  const name = entry.fullPath.substring(entry.fullPath.lastIndexOf("/") + 1);
  if (entry.publicDataPath !== undefined) {
    return {
      "url": entry.publicDataPath,
      "name": name
    };
  }
  let url = "./api/v1/debug/data/" + entry.execution + entry.fullPath;
  if (entry.source !== undefined) {
    url += "?source=" + entry.source;
  }
  return {
    "url": url,
    "name": name
  };
}

/**
 * Provide access to debug file content.
 */
export function useDebugData(metadata: DebugFileMetadata) {
  const [shouldFetch, setShouldFetch] =
    useState(metadata.size < FILE_PREVIEW_LIMIT);
  useEffect(() => {
    setShouldFetch(metadata.size < FILE_PREVIEW_LIMIT);
  }, [metadata]);
  const fetchWrap = useCallback((fetchFunction: FetchFunction) => {
    return fetchDebugData(fetchFunction, getDownloadUrl(metadata).url);
  }, [metadata])
  return {
    "shouldFetch": shouldFetch,
    "setShouldFetch": setShouldFetch,
    ...useFetch<string>(fetchWrap)
  };
}
