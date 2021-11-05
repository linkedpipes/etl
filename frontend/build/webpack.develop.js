const path = require("path");
const webpack = require("webpack");
const {merge} = require("webpack-merge");
const common = Object.assign({}, require("./webpack.common"));
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const ESLintPlugin = require("eslint-webpack-plugin");

// We need to use vue-style-loader for Vue but not for AngularJS.
// If we use vue-style-loader in AngularJS the content after import is not
// executed.
// We also need MiniCssExtractPlugin for AngularJS to make the style
// available as css file.
const vueStyleDirectories = [
  path.join(__dirname, "..", "client-debug"),
  path.join(__dirname, "..", "node_modules", "vuetify")
];

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
        "test": /\.css$/,
        "exclude": vueStyleDirectories,
        "use": [
          MiniCssExtractPlugin.loader,
          "css-loader"
        ]
      }, {
        "test": /\.css$/,
        "include": vueStyleDirectories,
        "use": [
          "vue-style-loader",
          "css-loader"
        ]
      }
    ]
  },
  "plugins": [
    new webpack.HotModuleReplacementPlugin(),
    new MiniCssExtractPlugin({
      "filename": "assets/styles/[name].[hash].css"
    }),
    // new ESLintPlugin({
    //   "extensions": ["js", "vue"],
    // }),
  ]
}, common);
