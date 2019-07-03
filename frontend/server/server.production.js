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
  const assetsDir = path.join(__dirname, "..", "dist", "assets");
  app.use("/assets", express.static(assetsDir));

  app.get("/", (req, res) => {
    res.sendFile(path.join(__dirname, "..", "dist", "client.html"));
  });

  app.get("/debug", (req, res) => {
    res.sendFile(path.join(__dirname, "..", "dist", "client-debug.html"));
  });

}

// Add event handlers.

process.on("SIGTERM", () => {
  server.close(() => {
    console.error("Closing server on 'SIGTERM'.");
    process.exit(0);
  });
});

process.on("SIGHUP", () => {
  console.error("Closing server on 'SIGHUP'.");
  process.exit(0);
});

process.on("SIGINT", () => {
  console.error("Closing server on 'SIGINT'.");
  process.exit(0);
});

process.on("exit", (code) => {
  console.error("About to exit with code:", code);
});

process.on("uncaughtException", (err) => {
  console.error("Caught exception:", err, err.stack);
});
