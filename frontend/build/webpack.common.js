const path = require("path");
const webpack = require("webpack");
const {VueLoaderPlugin} = require("vue-loader");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const {GitRevisionPlugin} = require("git-revision-webpack-plugin");
const gitRevisionPlugin = new GitRevisionPlugin();

module.exports = {
  "entry": {
    "client": path.join(__dirname, "..", "client", "index.js"),
    "client-react": path.join(__dirname, "..", "client-react", "index.jsx")
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
          "test": (module) => {
            // `module.resource` contains the absolute path of the file on disk.
            return (
              module.resource &&
              partOfPackage(
                module.resource,
                ["angular", "ng-"]
              ));
          },
          "filename": "assets/scripts/[name].[chunkhash].js",
          "name": "angular",
          "chunks": "all",
          "priority": 10
        },
        "jointjs": {
          "test": (module) => {
            // `module.resource` contains the absolute path of the file on disk.
            return (
              module.resource &&
              partOfPackage(
                module.resource,
                ["jointjs", "jquery", "lodash", "backbone"]
              ));
          },
          "filename": "assets/scripts/[name].[chunkhash].js",
          "name": "jointjs",
          "chunks": "all",
          "priority": 10
        },
        "triply": {
          "test": /[\\/]node_modules[\\/]@triply/,
          "filename": "assets/scripts/[name].[chunkhash].js",
          "name": "triply",
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
        "react": {
          "test": /[\\/]node_modules[\\/]react/,
          "filename": "assets/scripts/[name].[chunkhash].js",
          "name": "react",
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
    "extensions": [".js", ".jsx", ".vue", ".ts", ".tsx"],
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
        "test": /\.jsx?$/,
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
      "chunks": ["client", "commons", "angular", "jointjs", "triply"]
    }),
    new HtmlWebpackPlugin({
      "filename": "client-react.html",
      "template": path.join(__dirname, "..", "public", "client-react.html"),
      "inject": true,
      "chunks": ["client-react", "commons", "react"]
    }),
    new webpack.DefinePlugin({
      "__GIT_COMMIT__": JSON.stringify(gitRevisionPlugin.commithash())
    })
  ]
};

function partOfPackage(modulePath, packages) {
  if (!modulePath.includes("node_modules")) {
    return false;
  }
  const prefixes = packages.map(
    item => `${path.sep}node_modules${path.sep}${item}`
  );
  for (const prefix of prefixes) {
    if (modulePath.includes(prefix)) {
      return true;
    }
  }
  return false;
}
