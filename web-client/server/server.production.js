const express = require("express");
const path = require("path");
const logger = require("./logging");
const server = require("./server.common");

(function initialize() {
  const app = express();
  logger.info("Starting production server ...");
  server.initialize(app);
  initializeStatic(app);
  server.start(app);
}());

function initializeStatic(app) {
  app.use(express.static(path.join(__dirname, "..", "dist")));
  // All else to index.html to support non-root access.
  app.get("/*", (req, res) => {
    res.sendFile(path.join(__dirname, "..", "dist", "index.html"));
  });
}
