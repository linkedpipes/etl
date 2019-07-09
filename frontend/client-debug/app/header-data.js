import {asHumanReadableSize} from "../app-service/formats";

const publicData = {
  "title": "",
  "downloadUrl": undefined
};

export function getData() {
  return publicData;
}

export function onHome() {
  publicData["title"] = "LinkedPipes ETL : Debug View";
  publicData["downloadUrl"] = undefined;
}

export function onListing(execution, path, count) {
  publicData["title"] = execution + "/" + path +
    " (records count: " + count + ")";
  publicData["downloadUrl"] = undefined;
}

export function onDetail(execution, path, size, downloadUrl) {
  const humanReadableSize = asHumanReadableSize(size);
  publicData["title"] = execution + "/" + path + " (size: " + humanReadableSize + ")";
  publicData["downloadUrl"] = downloadUrl;
}

