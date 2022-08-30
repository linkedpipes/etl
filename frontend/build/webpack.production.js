const path = require("path");
const {merge} = require("webpack-merge");
const common = Object.assign({}, require("./webpack.common"));
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const CssMinimizerPlugin = require("css-minimizer-webpack-plugin");
const {CleanWebpackPlugin} = require("clean-webpack-plugin");
const CopyWebpackPlugin = require("copy-webpack-plugin");
const TerserPlugin = require("terser-webpack-plugin");

module.exports = merge(common, {
  "mode": "production",
  "devtool": "source-map",
  "output": {
    "filename": "assets/scripts/[name].[contenthash].js",
  },
  "optimization": {
    "minimizer": [
      new CssMinimizerPlugin(),
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
          {
            "loader": "css-loader",
            "options": {
              "sourceMap": true
            }
          },
        ]
      }
    ]
  },
  "plugins": [
    new CleanWebpackPlugin({}),
    new MiniCssExtractPlugin(
      {
      "filename": "assets/styles/[name].[contenthash].css"
      }
    ),
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
