define([
  "../template-service"
], function (_templateService) {
  "use strict";

  const ICONS = {
    "http://etl.linkedpipes.com/ontology/component/type/Extractor": "file_download",
    "http://etl.linkedpipes.com/ontology/component/type/Transformer": "transform",
    "http://etl.linkedpipes.com/ontology/component/type/Loader": "file_upload",
    "http://etl.linkedpipes.com/ontology/component/type/Executor": "call_split",
    "http://etl.linkedpipes.com/ontology/component/type/Quality": "help_outline"
  };

  function controller($scope, $mdDialog, templateService, filter,
                      designService) {

    $scope.templates = [];

    /**
     * Return array of component to add to the view list.
     *
     * @param component
     * @returns Array.
     */
    function transform(component) {
      const componentCore = templateService.getCoreTemplate(component);
      return {
        "label": component.label,
        "search": component.search,
        "parent": component.isCore ? "" : componentCore.label,
        "icon": ICONS[componentCore.type],
        "component": component,
        "order": 0,
        "visible": true
      };
    }

    /**
     * Return array of component to add to the view list. Multiple
     * instances can be returned for a single call.
     *
     * Use if the components should follow a certain other
     * component (source).
     *
     * @param component
     * @param source Source component.
     * @param sourcePort Source port.
     * @returns Array
     */
    function transformWithFilter(component, source, sourcePort) {
      const componentCore = templateService.getCoreTemplate(component);
      if (componentCore === undefined || componentCore.inputs.length === 0) {
        return [];
      }
      const result = [];
      componentCore.inputs.forEach(function (port) {
        if (areDataUnitsCompatible(sourcePort.content, port.content)) {
          const record = transform(component);
          record["portBinding"] = port.binding;
          record["portLabel"] = port.label;
          record["order"] = designService.getTemplatePriority(
            source.id, component.id);
          result.push(record);
        }
      });
      // Hyde port label if there is only one instance.
      if (result.length === 1) {
        result[0].portLabel = "";
      }
      return result;
    }

    /**
     * Check if given source can be connected to given target port.
     */
    function areDataUnitsCompatible(source, target) {
      return source === target;
    }

    $scope.onSelect = function (value) {
      $mdDialog.hide(value);
    };

    $scope.onCancel = function () {
      $mdDialog.hide(undefined);
    };

    $scope.$on("$routeChangeStart", function ($event, next, current) {
      $mdDialog.cancel();
    });

    $scope.$watch("searchLabel", (newValue, oldValue) => {
      if (newValue === oldValue) {
        return;
      }
      if (newValue === "") {
        // No filter, just set all to visible.
        $scope.templates.forEach((template) => {
          template.visible = true;
        });
        return;
      }
      const query = new RegExp(
        newValue.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, "\\$&"), "i");
      $scope.templates.forEach((template) => {
        template.visible = query.test(template.search);

      });
    });

    function init() {
      templateService.load().then(() => {
        const templatesList = templateService.getTemplatesList();
        if (filter.templateIri === undefined) {
          templatesList.forEach(function (item) {
            if (item.isInvalid) {
              return;
            }
            $scope.templates.push(transform(item));
          });
        } else {
          // Get source port.
          const sourceTemplate = templateService.getTemplate(
            filter.templateIri);
          const sourceCore = templateService.getCoreTemplate(
            sourceTemplate);
          var sourcePort;
          for (let i = 0; i < sourceCore.outputs.length; ++i) {
            if (sourceCore.outputs[i].binding ===
              filter.binding) {
              sourcePort = sourceCore.outputs[i];
              break;
            }
          }
          //
          if (sourcePort === undefined) {
            console.error("Error missing sourcePort, filter: ",
              filter, " template: ", sourceTemplate);
            // Add all.
            templatesList.forEach(function (item) {
              $scope.templates.push(transform(item));
            });
          } else {
            templatesList.forEach(function (item) {
              Array.prototype.push.apply($scope.templates,
                transformWithFilter(item, sourceTemplate,
                  sourcePort));
            });
          }
        }
      });
    }

    designService.initialize(init);

  }

  let _initialized = false;
  return function init(app) {
    if (_initialized) {
      return;
    }
    _initialized = true;
    _templateService(app);
    app.controller("components.templates.select.dialog",
      ["$scope", "$mdDialog", "template.service", "filter",
        "service.pipelineDesign", controller]);
  };

});

