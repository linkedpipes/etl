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

    const plugins = [];

    const ignore = [];

    return {
        "presets": presets,
        "plugins": plugins,
        "ignore": ignore
    }
};