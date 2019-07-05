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

export function onListing(execution, path) {
  publicData["title"] = execution + "/" + path;
  publicData["downloadUrl"] = undefined;
}

export function onDetail(execution, path, size, downloadUrl) {
  publicData["title"] = execution + "/" + path + " (size: " + size + "B)";
  publicData["downloadUrl"] = downloadUrl;
}