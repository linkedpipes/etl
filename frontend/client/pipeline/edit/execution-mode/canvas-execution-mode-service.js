((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    }
})(() => {

    let $canvas;

    let $actions;

    const data = {
        "disabled": false
    };

    function factory() {
        return {
            "bind": bind,
            "setEnabled": setEnabled,
            "ui": {}
        }
    }

    function setEnabled(enabled) {
        data.disabled = !enabled;
    }

    function onPointerClick(cell, event) {
        if (data.disabled) {
            return;
        }
        const component = $canvas.getComponentById(cell.model.id);
        if (component === undefined) {
            return;
        }
        if (event.target.getAttribute("magnet")) {
            $actions.onShowPortContent(
                component, event.target.getAttribute("port"));
        } else {
            $actions.onShowComponentExecutionDetail(component);
        }
    }

    function bind(_canvas, _actions) {
        $canvas = _canvas;
        $actions = _actions;

        const paper = $canvas.getPaper();
        paper.on("cell:pointerdown", onPointerClick);
    }

    let initialized = false;
    return function (app) {
        if (initialized) {
            return;
        }
        initialized = true;
        app.factory("canvas.execution-mode.service", factory);
    };

});
