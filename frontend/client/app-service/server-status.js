import {getJson} from "./http";

export const status = {
  "loaded": false,
  "fetching": false,
  "label": "LinkedPipes ETL"
};

const onLoadCallbacks = [];

(function initialize() {
  loadStatus();
})();

function loadStatus() {
  if (status.loaded || status.fetching) {
    return;
  }
  status.fetching = true;
  getJson("./api/v1/status").then((response) => {
    status.loaded = true;
    status.fetching = false;
    status.label = response.payload["instance"]["label"];
    onLoadCallbacks.forEach((callback) => {
      callback(status)
    });
  }).catch((error) => {
    status.fetching = false;
    console.warn("Can't fetch server status.", error);
  })
}

export function getStatus() {
  if (!status.loaded) {
    loadStatus();
  }
  return status;
}

export function onLoad(callback) {
  if (status.loaded) {
    callback(status);
  } else {
    onLoadCallbacks.push(callback);
  }
}