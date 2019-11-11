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
  publicData["title"] =
    removeDataUnitDirectory(path) +
    " (records count: " + count + ")";
  publicData["downloadUrl"] = undefined;
}

function removeDataUnitDirectory(path) {
  if (path === "/") {
    return path;
  }
  return path.substr(4);
}

export function onDetail(execution, path, size, downloadUrl) {
  publicData["title"] =
    removeDataUnitDirectory(path) +
    " (size: " + asHumanReadableSize(size) + ")";
  publicData["downloadUrl"] = downloadUrl;
}

