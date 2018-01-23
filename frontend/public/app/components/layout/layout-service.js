define([], function () {

    function service() {
        this.color = "#2196f3";
        this.title = "LinkedPipes ETL";
    }

    let _initialized = false;
    return function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        app.service("layout-service", service);
    };

});