define([], function () {

  function service() {
    this.color = "#2196f3";
  }

  let _initialized = false;
  return function init(app) {
    if (_initialized) {
      return;
    }
    _initialized = true;
    app.service("app-layout.service", service);
  };

});