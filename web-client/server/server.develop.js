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
  initializeWebpack(app);
  server.start(app);
}());

function initializeWebpack(app) {
  const webpackCompiler = webpack(config);
  app.use(webpackMiddleware(webpackCompiler, {
    "publicPath": config.output.publicPath.substr(1),
    "stats": {
      "colors": true,
      "chunks": false,
    },
  }));
  app.use(webpackHotMiddleware(webpackCompiler));
}
