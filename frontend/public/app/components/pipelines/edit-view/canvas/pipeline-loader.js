((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    }
})(() => {

    function loadPipeline(canvas, pipeline) {
        const api = canvas.getUserApi();
        const components = api.getComponents(pipeline);
        const dataLinks = api.getDataLinks(pipeline);
        const runAfterLinks = api.getRunAfter(pipeline);
        setInitialViewPosition(canvas, components);
        return loadResources(canvas, components, dataLinks, runAfterLinks);
    }

    function loadResources(
        canvas, components, dataLinks, runAfterLinks) {
        const {"cells": componentCells, "missing": missing} =
            loadComponents(canvas, components);

        const cellsToAdd = [].concat(
            componentCells,
            loadLinks(canvas, dataLinks,
                canvas.createPortLinkCell.bind(canvas)),
            loadLinks(canvas, runAfterLinks,
                canvas.createComponentLinkCell.bind(canvas))
        );

        canvas.addCells(cellsToAdd);

        return Promise.resolve({
            "missingTemplates": missing
        });
    }

    function setInitialViewPosition(canvas, components) {
        const api = canvas.getUserApi();
        let x = undefined;
        let y = undefined;
        components.forEach((component) => {
            // There is no position or this one is the most left one.
            const isLeftMost =
                x === undefined || x > api.getComponentX(component);
            // It's almost at the very left, but higher.
            const isReasonablyLeft =
                Math.abs(x - api.getComponentX(component)) < 100 &&
                y > api.getComponentY(component);
            if (isLeftMost || isReasonablyLeft) {
                x = api.getComponentX(component);
                y = api.getComponentY(component);
            }
        });
        canvas.setOrigin(-(x - 50), -(y - 100));
    }

    function loadComponents(canvas, components) {
        const api = canvas.getUserApi();
        const cells = [];
        const missing = {};
        components.forEach((component) => {
            const templateIri = api.getComponentTemplateIri(component);
            const template = api.getTemplate(templateIri);
            if (template === undefined) {
                if (missing[templateIri] === undefined) {
                    missing[templateIri] = [];
                }
                missing[templateIri].push(component);
                return;
            }
            const cell = canvas.createComponentCell(
                template, component);
            canvas.registerComponentCell(component, cell);
            cells.push(cell);
        });
        return {
            "cells": cells,
            "missing": missing
        }
    }

    function loadLinks(canvas, connections, factoryFnc) {
        const api = canvas.getUserApi();
        const cells = [];
        connections.forEach((connection) => {
            const vertices = api.getConnectionVertices(connection);
            const cell = factoryFnc(connection, vertices);
            if (cell === undefined) {
                console.log("Invalid connection:", connection);
                return;
            }
            canvas.registerLinkCell(connection, cell);
            cells.push(cell);
        });
        return cells;
    }

    return {
        "loadPipeline": loadPipeline,
        /**
         * Add resources (links, components, ... ) to existing pipeline.
         * Similar to loadPipeline but do not set position.
         */
        "loadResources": loadResources
    }

});
