define([
    "@triply/yasqe",
    "@triply/yasqe/build/yasqe.min.css",
    "./yasqe-editor.css"
  ],
  (YASQE) => {

    /**
     * Initialize only one way binding.
     */
    function createYasqe(element, $scope) {
      // -> https://triply.cc/docs/yasgui-api#yasqe-api
      const yasqe = new YASQE(element, {
        // Disable buttons.
        "showQueryButton": false,
        "createShareableLink": null,
        // Does not work well with the dialogs.
        "resizeable": false,
        // Disable persistence.
        "persistenceId": null,
      });
      yasqe.setSize("100%", "100%");

      yasqe.on("change", () => {
        $scope.ngModel = yasqe.getValue();
        return false;
      });

      return yasqe;
    }

    function directive() {
      return {
        "restrict": "E",
        "scope": {
          "ngModel": "=ngModel",
          "ngDisabled": "=ngDisabled"
        },
        "template": "",
        "link": ($scope, element) => {
          element.ready(() => {
            // element is wrapped in JQLite but we need the element.
            const yasqe = createYasqe(element[0], $scope);
            yasqe.setValue($scope.ngModel);
            yasqe.setOption("readOnly", $scope.ngDisabled);

            $scope.$watch("ngDisabled", (newValue, oldValue) => {
              yasqe.setOption("readOnly", newValue);
              if (newValue) {
                element.addClass("readOnly");
              } else {
                element.removeClass("readOnly");
              }
            });

          });
        }
      };
    }

    directive.$inject = [];

    let _initialized = false;
    return function init(app) {
      if (_initialized) {
        return;
      }
      _initialized = true;

      app.directive("lpYasqe", directive);
    };

  });

//
