//
// Add pipeline edit functionality to a pipeline canvas.
//

define([
    'jquery',
    'jointjs',
    'backbone',
    'lodash'
], function (jQuery, joint, Backbone, _) {

    var doNothing = function () {};

    var service = {
        /**
         * Define public API listeners that corresponds to actions
         * that are not be handled by this directive.
         */
        'API': {
            'onEdit': doNothing,
            'onDebug': doNothing,
            'onAddComponent': doNothing,
            'onImportPipeline': doNothing,
            'onMapping': doNothing,
            'onEnableDisable': doNothing,
            /**
             * Return true if mapping function is available for
             * given component.
             */
            'mappingAvailable': function (component) {
                return false;
            }
        },
        /**
         * Information about canvas.
         */
        'canvas': {
            'API': void 0,
            'DOM': void 0
        },
        /**
         * Reference to the pipelineCanvas service.
         */
        'pipelineCanvas': void 0,
        /**
         * Enabled/disabled.
         */
        'enabled': false,
        /**
         * Contains informations about the component menu.
         */
        'componentMenu': {
            'DOM': void 0,
            'visible': false,
            /**
             * View as originally passed to the showComponentMenu.
             */
            'view': void 0,
            'component': void 0,
            /**
             * If True menu was visible before panning event starts.
             */
            'panning': false,
            'mapping': {
                'DOM': void 0
            }
        },
        /**
         * Information about blank space menu.
         */
        'blankMenu': {
            'DOM': void 0,
            'visible': false,
            'x': 0,
            'y': 0
        },
        /**
         * List of currently selected components. As updated by
         * the canvasDirective.
         */
        'selection': [],
        /**
         * Used to monitor add "run before" connection actions.
         */
        'prerequisite': {
            'active': false,
            /**
             * ID of source component.
             */
            'source': void 0
        }
    };

    service.getComponentMenuDom = function() {
        if (this.componentMenu.DOM === undefined) {
            this.componentMenu.DOM = jQuery('#componentMenu');
        }
        return this.componentMenu.DOM;
    };

    service.getComponentMenuMappingDom = function() {
        if (this.componentMenu.mapping.DOM === undefined) {
           this.componentMenu.mapping.DOM = jQuery('#mapping');
        }
        return this.componentMenu.mapping.DOM;
    };

    service.getCanvasDom = function() {
        if (this.canvas.DOM === undefined) {
            this.canvas.DOM = jQuery('#canvas');
        }
        return this.canvas.DOM;
    };

    service.getBlankMenuDom = function() {
        if (this.blankMenu.DOM === undefined) {
            this.blankMenu.DOM = jQuery('#blankMenu');
        }
        return this.blankMenu.DOM;
    };

    service.showComponentMenu = function (view, component) {
        // We can't use bounding box of the cell as it includes
        // the labels, instead we need to get
        var rectCell = this.canvas.API.getGraph().getCell(view.model.id);
        if (rectCell === undefined) {
            console.warn('showComponentMenu', component, rectCell);
            return;
        }
        // Compute component bounding box.
        var paperOffset = this.canvas.API.getPaper().viewport.getCTM();
        var boundingBox = rectCell.getBBox();
        boundingBox.x += paperOffset.e;
        boundingBox.y += paperOffset.f;
        // Update position.
        var canvasPosition = this.getCanvasDom().position();
        var componentMenuDom = this.getComponentMenuDom();
        componentMenuDom.css('left',
                boundingBox.x + canvasPosition.left + 20);
        componentMenuDom.css('top',
                boundingBox.y + canvasPosition.top + 0);
        // Update size.
        service.menuComponentUpdateHeight(boundingBox.height);
        // Show.
        componentMenuDom.css('display', 'inline');
        this.componentMenu.view = view;
        this.componentMenu.component = component;
        this.componentMenu.visible = true;
        // Mapping
        if (this.API.mappingAvailable(component)) {
            this.getComponentMenuMappingDom().css('display', 'inline');
        } else {
            this.getComponentMenuMappingDom().css('display', 'none');
        }
    };

    service.menuComponentUpdateHeight = function (height) {
        var bottomMenu = this.getComponentMenuDom().find('#bottomBar');
        bottomMenu.css('top', (height - 45) + 'px');
    };

    service.hydeComponentMenu = function () {
        this.getComponentMenuDom().css('display', 'none');
        this.componentMenu.visible = false;
    };

    service.showEmptySpaceMenu = function (event, x, y) {
        var blankMenuDom = this.getBlankMenuDom();
        blankMenuDom.css('left', event.clientX);
        blankMenuDom.css('top', event.clientY);
        blankMenuDom.css('display', 'inline');
        // Save position of the empty space menu.
        this.blankMenu.x = x;
        this.blankMenu.y = y;
        this.blankMenu.visible = true;
    };

    service.hydeEmptySpaceMenu = function () {
        this.getBlankMenuDom().css('display', 'none');
        this.blankMenu.visible = false;
    };

    service.onPointerClick = function (view) {
        if (!this.enabled) {
            return;
        }
        var component = this.pipelineCanvas.getComponent(view.model.id);
        if (component === undefined) {
            // Can happen if component has been removed
            // for example for edges to blank.
            return;
        }
        if (this.prerequisite.active) {
            this.prerequisite.active = false;
            this.pipelineCanvas.insertRunAfter(
                    this.pipelineCanvas.getComponent(this.prerequisite.source),
                    component);
        }
        if (this.blankMenu.visible) {
            service.hydeEmptySpaceMenu();
        }
        this.showComponentMenu(view, component);
    };

    service.onPointerDoubleClick = function () {
        if (!this.enabled) {
            return;
        }
        // We need the item to be selected.
        this.onEditComponent();
    };

    service.onEmptyClick = function (event, x, y) {
        if (!this.enabled) {
            return;
        }
        if (this.prerequisite.active) {
            this.prerequisite.active = false;
            return;
        }
        if (this.componentMenu.visible) {
            this.hydeComponentMenu();
            return;
        }
        if (this.blankMenu.visible) {
            service.hydeEmptySpaceMenu();
        } else {
            service.showEmptySpaceMenu(event, x, y);
        }
    };

    service.onMoveStart = function () {
        if (!this.enabled) {
            return;
        }
        if (this.componentMenu.visible) {
            this.hydeComponentMenu();
            this.componentMenu.panning = true;
        } else {
            this.componentMenu.panning = false;
        }
        if (this.blankMenu.visible) {
            service.hydeEmptySpaceMenu();
        }
    };

    service.onMoveEnd = function () {
        if (!this.enabled) {
            return;
        }
        if (this.componentMenu.panning) {
            this.showComponentMenu(this.componentMenu.view,
                    this.componentMenu.component);
            this.componentMenu.panning = false;
        }
    };

    service.setEnabled = function (enabled) {
        this.enabled = enabled;
        if (!enabled) {
            this.hydeComponentMenu();
            this.hydeEmptySpaceMenu();
            //
            this.componentMenu.panning = false;
            this.prerequisite.active = false;
        }
    };

    service.bind = function (canvas, pipelineCanvas) {
        this.canvas.API = canvas;
        this.pipelineCanvas = pipelineCanvas;

        canvas.getPaper().on('cell:pointerup',
                this.onPointerClick.bind(this));

        canvas.getPaper().on('lp:emptyclick',
                this.onEmptyClick.bind(this));

        canvas.getPaper().on('lp:panningstart',
                this.onMoveStart.bind(this));

        canvas.getPaper().on('lp:panningend',
                this.onMoveEnd.bind(this));

        canvas.getPaper().on('cell:pointerdblclick',
                this.onPointerDoubleClick.bind(this));

        canvas.getPaper().on('lp:component:changed', function (iri) {
            // This may affect the menu somehow.
            if (this.componentMenu.visible &&
                    iri === this.componentMenu.component['@id']) {
                // Refresh menu.
                this.showComponentMenu(this.componentMenu.view,
                        this.componentMenu.component);
            }
        }.bind(this));

        canvas.getGraph().on('change:size', function (cell, size) {
            if (cell !== undefined &&
                    cell.id === this.componentMenu.view.model.id &&
                    this.componentMenu.visible) {
                this.menuComponentUpdateHeight(size.height);
            }
        }.bind(this));

        // Watch for cell drag and drop movement.

        var pointerdown = false;
        var movereported = false;

        canvas.getPaper().on('cell:pointerdown', function (event) {
            pointerdown = true;
            movereported = false;
        }.bind(this));

        canvas.getPaper().on('cell:pointermove', function (event) {
            if (pointerdown && !movereported) {
                this.onMoveStart();
                movereported = true;
            }
        }.bind(this));

        canvas.getPaper().on('cell:pointerup', function (event) {
            if (movereported) {
                this.onMoveEnd();
            }
            pointerdown = false;
            movereported = false;
        }.bind(this));

        // Watch for changes in the selection.

        canvas.getPaper().on('lp:selected', function (cell) {
            this.selection.push(cell);
        }.bind(this));

        canvas.getPaper().on('lp:deselected', function (cell) {
            this.selection.splice(this.selection.indexOf(cell), 1);
        }.bind(this));

        // In case of drag a connectio into an empty space the conneciton
        // is created and then removed.
        canvas.getGraph().on('remove', function (cell) {
            if (cell instanceof joint.dia.Link) {
                /// If the link ends in the blank, the target is not
                // a reference to a component but a point (x, y)
                // on the paper.
                if (cell.attributes.target.x) {
                    this.onConnectionToBlank(cell,
                            cell.attributes.target.x,
                            cell.attributes.target.y);
                }
            }
        }.bind(this));

    };

    service.onEditComponent = function () {
        var component = this.pipelineCanvas.getResource(
                this.componentMenu.view.model.id);
        var cell = this.canvas.API.getGraph().getCell(
                this.componentMenu.view.model.id);
        this.API.onEdit(cell, component);
    };

    service.onDeleteComponent = function () {
        // Remove the cell from the canvas, this trigger the 'remove'
        // event, which takes care about removal of the component from model.
        if (this.componentMenu.view !== undefined) {
            this.hydeComponentMenu();
            this.pipelineCanvas.delete(this.componentMenu.view.model.id);
        }
    };

    service.onCopyComponent = function () {
        this.pipelineCanvas.clone(this.componentMenu.view.model.id);
    };

    service.onEnableDisable = function() {
        var component = this.pipelineCanvas.getResource(
                this.componentMenu.view.model.id);
        this.API.onEnableDisable(component);
    };

    service.onPrerequisiteComponent = function () {
        // TODO Update to enable link drag.
        this.prerequisite.active = true;
        this.prerequisite.source = this.componentMenu.view.model.id;
    };

    service.onDebugToComponent = function () {
        var component = this.pipelineCanvas.getResource(
                this.componentMenu.view.model.id);
        this.API.onDebug(component);
    };

    service.onMappingComponent = function () {
        var component = this.pipelineCanvas.getResource(
                this.componentMenu.view.model.id);
        this.API.onMapping(component);
    };

    service.onAddComponent = function () {
        this.hydeEmptySpaceMenu();
        this.API.onAddComponent(this.blankMenu.x, this.blankMenu.y);
    };

    service.onImportPipeline = function () {
        this.hydeEmptySpaceMenu();
        this.API.onImportPipeline(this.blankMenu.x, this.blankMenu.y);
    };

    service.onConnectionToBlank = function (link, x, y) {
        var component = this.pipelineCanvas.getResource(
                link.attributes.source.id);
        this.API.onAddComponent(x, y, {
            'component': component,
            'port': link.attributes.source.port
        });
    };

    service.onCreateTemplate = function() {
        var component = this.pipelineCanvas.getResource(
            this.componentMenu.view.model.id);
        var cell = this.canvas.API.getGraph().getCell(
            this.componentMenu.view.model.id);
        this.API.onCreateComponent(cell, component);
    }

    function directiveFactory() {
        // Used to store directive data outstide the scope.
        var instance;
        jQuery.extend({}, service);
        return {
            'restrict': 'E',
            'scope': {'api': '='},
            'templateUrl': 'app/components/pipelineEditDirective/pipelineEditHtml.html',
            'link': function ($scope, element, attrs) {
                instance = jQuery.extend($scope.api, service);
                // Get references.
                instance.canvas.DOM = void 0;
                instance.componentMenu.DOM = void 0;
                instance.componentMenu.mapping.DOM = void 0;
                instance.blankMenu.DOM = void 0;
                // Callback when ready.
                if (instance.onLink) {
                    instance.onLink();
                }
            }
        };
    }

    return function (app) {
        app.directive('lpPipelineEdit', [directiveFactory]);
    };

});
