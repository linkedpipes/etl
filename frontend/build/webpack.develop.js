const webpack = require("webpack");
const {merge} = require("webpack-merge");
const common = Object.assign({}, require("./webpack.common"));
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

module.exports = merge({
  "mode": "development",
  "devtool": "inline-source-map",
  "entry": {
    "webpack-hot-middleware": "webpack-hot-middleware/client"
  },
  "devServer": {
    "hot": true
  },
  "module": {
    "rules": [
      {
        "test": /\.css$/i,
        "use": [
          MiniCssExtractPlugin.loader,
          "css-loader"
        ]
      }
    ]
  },
  "plugins": [
    new webpack.HotModuleReplacementPlugin(),
    new MiniCssExtractPlugin({
      "filename": "assets/styles/[name].[contenthash].css"
    }),
  ]
}, common);
