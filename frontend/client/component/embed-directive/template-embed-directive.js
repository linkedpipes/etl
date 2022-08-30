/**
 * Directive for embedding controller and HTML template.
 */

define(["angular"], function (angular) {
  "use strict";

  // We had used RequireJS, which is no longer true.
  // But the components are still using define wrap function,
  // so we need to provide it.
  window.define = (modules, fnc) => {
    if (modules.size > 0) {
      console.warn("Ignoring module requirements:", modules);
    }
    return fnc();
  };

  function directive($templateRequest, $controller, $compile, $cacheFactory, $http, $q) {

    const cacheFactory = $cacheFactory("dialog-cache");

    function link(scope, element) {
      console.time("Loading dialog");
      loadControllerFunction(scope.js).then((controllerFunction) => {
        // Load the HTML.
        $templateRequest(scope.html).then((htmlTemplate) => {
          // Create a DOM from given template.
          const templateElement = angular.element(htmlTemplate);
          // Instantiate a controller and pass given variables
          // (locals to bind). We have to make sure that the
          // content is one of those variables to bind.
          scope.locals.$scope = scope.scope;
          $controller(controllerFunction, scope.locals);
          // Compile the element to the template function.
          const templateFunction = $compile(templateElement);
          // Bind controller to the template.
          const modalDomEl = templateFunction(scope.scope);
          // Add new HTML to the DOM.
          element.append(modalDomEl);
          if (scope.onLoad) {
            console.timeEnd("Loading dialog");
            scope.onLoad();
          }
        });
      });

      function loadControllerFunction(path) {
        const cached = cacheFactory.get(path);
        if (cached === undefined) {
          return $http.get(scope.js).then((response) => {
            const jsCodeAsStr = response.data;
            const controllerFunction = eval(jsCodeAsStr);
            cacheFactory.put(path, controllerFunction);
            return controllerFunction;
          });
        } else {
          const deferred = $q.defer();
          deferred.resolve(cached);
          return deferred.promise;
        }

      }

    }

    return {
      "restrict": "A",
      "scope": {
        // Path to the JS script file.
        "js": "=",
        // Path to the HTML file.
        "html": "=",
        // Scope given to the embedded component.
        "scope": "=",
        // Map of additional properties to pass the embedded component
        "locals": "=",
        // Callback.
        "onLoad": "&"
      },
      "link": link
    };
  }

  let _initialized = false;
  return function init(app) {
    if (_initialized) {
      return;
    }
    _initialized = true;
    app.directive("lpEmbed",
      ["$templateRequest", "$controller", "$compile", "$cacheFactory", "$http", "$q", directive]);
  };

});
