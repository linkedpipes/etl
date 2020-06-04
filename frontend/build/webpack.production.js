const path = require("path");
const merge = require("webpack-merge");
const common = Object.assign({}, require("./webpack.common"));
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const OptimizeCSSAssetsPlugin = require("optimize-css-assets-webpack-plugin");
const {CleanWebpackPlugin} = require("clean-webpack-plugin");
const CopyWebpackPlugin = require("copy-webpack-plugin");
const TerserPlugin = require("terser-webpack-plugin");

module.exports = merge(common, {
  "mode": "production",
  "devtool": "source-map",
  "output": {
    "filename": path.join("assets", "scripts", "[name].[chunkhash].js"),
  },
  "optimization": {
    "minimizer": [
      new OptimizeCSSAssetsPlugin({}),
      new TerserPlugin({
        "parallel": true,
        "sourceMap": true,
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
      "filename": path.join("assets", "styles", "[name].[chunkhash].css")
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
});
