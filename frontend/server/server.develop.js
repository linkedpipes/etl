const path = require("path");
const express = require("express");
const webpack = require("webpack");
const webpackMiddleware = require("webpack-dev-middleware");
// https://github.com/webpack-contrib/webpack-hot-middleware
const webpackHotMiddleware = require("webpack-hot-middleware");
const config = require("../build/webpack.develop.js");
const server = require("./server.common");
const logger = require("./logging");

(function initialize() {
  logger.info("Starting develop server ...");
  const app = express();
  server.initialize(app);
  const webpackCompiler = initializeWebpack(app);
  initializeStatic(app);
  initializeHtmlFiles(app, webpackCompiler);
  server.start(app);
}());

function initializeWebpack(app) {
  const webpackCompiler = webpack(config);
  const devMiddleware = webpackMiddleware(webpackCompiler, {
    "publicPath": getPublicOutputPath(),
    "stats": {
      "colors": true,
      "chunks": false,
    },
  });
  app.use(devMiddleware);
  app.use(webpackHotMiddleware(webpackCompiler));
  return webpackCompiler;
}

function getPublicOutputPath() {
  return config.output.publicPath.substr(1);
}

function initializeStatic(app) {
  const assetsDir = path.join(__dirname, "..", "public", "assets");
  app.use("/assets", express.static(assetsDir));
}

function initializeHtmlFiles(app, webpackCompiler) {
  // webpackMiddleware will server files as ./index.html and ./debug.html,
  // but we want it to be '/' and '/debug'
  const webpackFileSystem = webpackCompiler.outputFileSystem;

  const clientPath = path.join(webpackCompiler.outputPath, "client.html");
  app.get("/", (req, res) => {
    webpackFileSystem.readFile(clientPath, (err, result) => {
      res.set("content-type", "text/html");
      res.send(result);
      res.end();
    });
  });

  const clientDebugPath =
    path.join(webpackCompiler.outputPath, "client-debug.html");
  app.get("/debug", (req, res) => {
    webpackFileSystem.readFile(clientDebugPath, (err, result) => {
      res.set("content-type", "text/html");
      res.send(result);
      res.end();
    });
  });

  const clientReactPath =
    path.join(webpackCompiler.outputPath, "client-react.html");
  app.get("/react", (req, res) => {
    webpackFileSystem.readFile(clientReactPath, (err, result) => {
      res.set("content-type", "text/html");
      res.send(result);
      res.end();
    });
  });

}