const path = require("path");
const webpack = require("webpack");
const merge = require("webpack-merge");
const common = Object.assign({}, require("./webpack.common"));
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

// We need to use vue-style-loader for Vue but not for AngularJS.
// If we use vue-style-loader in AngularJS the content after import is not
// executed.
// We also need MiniCssExtractPlugin for AngularJS to make the style
// available as css file.
const vueStyleDirectories = [
  path.join(__dirname, "..", "client-debug"),
  path.join(__dirname, "..", "node_modules", "vuetify")
];

module.exports = merge(common, {
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
      }, {
        // Apply linter during run development.
        "enforce": "pre",
        "test": /\.(js|vue)$/,
        "loader": "eslint-loader",
        "exclude": /node_modules/
      }
    ]
  },
  "plugins": [
    new webpack.HotModuleReplacementPlugin(),
    new MiniCssExtractPlugin({
      "filename": "assets/styles/[name].[hash].css"
    }),
  ]
});
