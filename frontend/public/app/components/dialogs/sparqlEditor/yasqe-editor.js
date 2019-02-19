define(["yasqe"], (YASQE) => {

    console.log("YASQE", YASQE.defaults.autocompleters);
    console.log("Autocompletion ", YASQE.Autocompleters);

    /**
     * Initialize only one way binding.
     */
    function createYasqe(element, $scope) {
        const settings = {
            "createShareLink": null,
            "consumeShareLink": null,
            "persistent": null
        };
        const yasqe = YASQE(element, settings);
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
                    const yasqe = createYasqe(element, $scope);
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
