const path = require("path");
const webpack = require("webpack");
const {VueLoaderPlugin} = require("vue-loader");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const GitRevisionPlugin = require("git-revision-webpack-plugin");
const gitRevisionPlugin = new GitRevisionPlugin();

module.exports = {
  "entry": {
    "client": path.join(__dirname, "..", "client", "index.js"),
    "client-debug": path.join(__dirname, "..", "client-debug", "index.js")
  },
  "output": {
    "path": path.join(__dirname, "..", "dist"),
    "filename": "assets/scripts/[name].js",
    "publicPath": "./"
  },
  "optimization": {
    "splitChunks": {
      "cacheGroups": {
        "angular": {
          "test": /[\\/]node_modules[\\/]angular/,
          "filename": "assets/scripts/[name].[chunkhash].js",
          "name": "angular",
          "chunks": "all",
          "priority": 10
        },
        "vue": {
          "test": /[\\/]node_modules[\\/]vue/,
          "filename": "assets/scripts/[name].[chunkhash].js",
          "name": "vue",
          "chunks": "all",
          "priority": 10
        },
        "commons": {
          "test": /[\\/]node_modules[\\/]/,
          "filename": "assets/scripts/[name].[chunkhash].js",
          "name": "commons",
          "chunks": "all"
        }
      },
    },
  },
  "resolve": {
    "modules": ["node_modules"],
    "extensions": [".js", ".vue", ".ts"],
    "alias": {
      "@client": path.resolve("client"),
      "@client-debug": path.resolve("client-debug")
    }
  },
  "module": {
    "rules": [
      {
        "test": /\.vue$/,
        "use": "vue-loader"
      }, {
        "test": /\.js$/,
        "use": "babel-loader"
      },
      {
        "test": /\.tsx?$/,
        "use": "ts-loader",
        "exclude": /node_modules/
      },
      {
        "test": /\.html$/,
        "use": "ng-cache-loader?-url&prefix=[dir]/[dir]",
        "exclude": [
          path.join(__dirname, "..", "public",)
        ]
      }
    ]
  },
  "plugins": [
    new VueLoaderPlugin(),
    new HtmlWebpackPlugin({
      "filename": "client.html",
      "template": path.join(__dirname, "..", "public", "client.html"),
      "inject": true,
      "chunks": ["client", "commons", "angular"]
    }),
    new HtmlWebpackPlugin({
      "filename": "client-debug.html",
      "template": path.join(__dirname, "..", "public", "client-debug.html"),
      "inject": true,
      "chunks": ["client-debug", "webpack-hot-middleware", "commons", "vue"]
    }),
    new webpack.DefinePlugin({
      "__GIT_COMMIT__": JSON.stringify(gitRevisionPlugin.commithash())
    })
  ]
};