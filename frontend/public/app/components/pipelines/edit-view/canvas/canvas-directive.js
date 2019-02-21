/**
 * Use canvas-service to access functionality of this directive.
 *
 * TODO Split link function.
 */
define([
    "jquery",
    "jointjs",
    "backbone",
    "lodash",
    "./selection",
    "./scrollable",
    "./events"
], function (jQuery, joint, Backbone, _, selection, scrollable, EVENTS) {

    function Paper(graph, element, interactiveFnc, connectionValidator) {
        // TODO Consider joint.dia.FastPaper -> https://jsfiddle.net/kumilingus/e3m4kjqa/
        return new joint.dia.Paper({
            "async": true,
            "el": element,
            "width": "100%",
            "height": "100%",
            "model": graph,
            "gridSize": 1,
            "linkPinning": false,
            "snapLinks": {
                "radius": 50
            },
            "defaultLink": new joint.dia.Link({
                "attrs": {
                    ".marker-target": {"d": "M 10 0 L 0 5 L 10 10 z"}
                }
            }),
            "validateConnection": connectionValidator,
            "markAvailable": true,
            "interactive": interactiveFnc
        });
    }

    function directive($service) {
        return {
            "restrict": "E",
            "template": "",
            "link": ($scope, element) => link($service, $scope, element)
        };
    }

    directive.$inject = ["canvas.service"];

    function link($service, $scope, element) {

        const service = {
            /**
             * In case of button down on empty space we can
             * initialize scroll, or we can fire onEmptySpaceClick.
             * This structure is used to store information about
             * empty space click and detect which possibility to
             * choose.
             */
            "moved": false,
            /**
             * True if pointer went down on the blank space.
             */
            "downblank": false,
            /**
             * Interactive mode.
             */
            "interactive": true,
            /**
             * Default validator.
             */
            "connectionValidator": () => true
        };

        const graph = new joint.dia.Graph;

        const paper = new Paper(
            graph, element, () => service.interactive, validatorProxy);

        function validatorProxy() {
            return service.connectionValidator.apply(undefined, arguments);
        }

        selection.bind(paper, graph);

        const scrollablePaper = scrollable.wrap(paper, service);

        scrollablePaper.$el.css({
            "width": "100%",
            "height": "100%"
        }).appendTo(element);

        paper.on("blank:pointerdown", function (event) {
            service.downblank = true;
            scrollablePaper.startPanning(event);
        });

        paper.on("blank:pointerup", function (event, x, y) {
            if (service.moved || !service.downblank) {
                return;
            }
            paper.trigger(EVENTS.selectionClean);
            paper.trigger(EVENTS.emptyClick, event, x, y);
            service.downblank = false;
        });

        paper.on("cell:pointerdown", function (view) {
            if (!service.interactive) {
                return;
            }
            if (view.model.getBBox) {
                selection.addToSelection(view);
            }
        });

        paper.el.oncontextmenu = ignoreEvent;

        const canvas = {
            "getGraph": () => graph,
            "getPaper": () => paper,
            "setInteractive": (interactive) => {
                service.interactive = interactive;
                if (!service.interactive) {
                    paper.trigger(EVENTS.selectionClean);
                }
            },
            "setConnectionValidator": (validator) => {
                service.connectionValidator = validator;
            },
            "setOrigin": (x, y) => {
                paper.setOrigin(x, y);
                scrollablePaper.positionX = x;
                scrollablePaper.positionY = y;
            }
        };

        $service.bindDirective(canvas);
    }

    function ignoreEvent(event) {
        event.preventDefault();
    }

    let initialized = false;
    return function (app) {
        if (initialized) {
            return;
        }
        initialized = true;
        app.directive("lpCanvas", directive);
    };

});
