module.exports = (api) => {

  api.cache.using(() => process.env.NODE_ENV);

  const presets = [
    [
      "@babel/preset-env",
      {
        "targets": {
          "chrome": 70
        }
      }
    ]
  ];

  const plugins = [
    // Angular use names for dependency injection, but they are mangled.
    // So we use this plugin to get them back via annotations.
    "angularjs-annotate"
  ];

  const ignore = [

  ];

  return {
    "presets": presets,
    "plugins": plugins,
    "ignore": ignore
  }
};