import {
  DebugEntry,
  DebugMetadata,
  DebugMetadataList, isDebugFileEntry
} from "./execution-debug-model";

export function jsonToDebugMetadataList(
  execution: string, path: string, source: string | undefined, response: any
): DebugMetadataList {
  // The original data contain multiple record for ambiguous entries.
  // For example if for given file there are files and two directories
  // with the same name we get that information. For the user-interface
  // we need only information that there are two files and a directory.
  const visited = new Set();
  const fistTimeSee = (value:any) => {
    if (visited.has(value)) {
      return false;
    }
    visited.add(value);
    return true;
  }

  const data = (response.data ?? []) as DebugEntry[];
  const metadata = response.metadata as DebugMetadata;
  return {
    "data": data
      .filter(item => isDebugFileEntry(item) || fistTimeSee(item.name))
      .map((item) => ({
      ...item,
      "fullPath": path + "/" + item.path,
      "execution": execution,
    })),
    "metadata": {
      ...metadata,
      "execution": execution,
      "fullPath": path,
      "source": source,
    },
  };
}
