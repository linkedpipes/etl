const path = require("path");
const {merge} = require("webpack-merge");
const common = Object.assign({}, require("./webpack.common"));
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const OptimizeCSSPlugin = require("css-minimizer-webpack-plugin");
const {CleanWebpackPlugin} = require("clean-webpack-plugin");
const CopyWebpackPlugin = require("copy-webpack-plugin");
const TerserPlugin = require("terser-webpack-plugin");

module.exports = merge({
  "mode": "production",
  "devtool": "source-map",
  "output": {
    "filename": "assets/scripts/[name].[chunkhash].js",
  },
  "optimization": {
    "minimizer": [
      new OptimizeCSSPlugin(),
      new TerserPlugin({
        "terserOptions": {
          "ecma": 6,
        },
      })
    ]
  },
  "module": {
    "rules": [
      {
        "test": /\.css$/,
        "use": [
          MiniCssExtractPlugin.loader,
          "css-loader"
        ]
      }
    ]
  },
  "plugins": [
    new CleanWebpackPlugin({}),
    new MiniCssExtractPlugin({
      "filename": "assets/styles/[name].[chunkhash].css"
    }),
    new CopyWebpackPlugin({
      "patterns": [
        {
          "from": path.join(__dirname, "..", "public", "assets"),
          "to": path.join(__dirname, "..", "dist", "assets"),
        },
      ],
    }),
  ]
}, common);
