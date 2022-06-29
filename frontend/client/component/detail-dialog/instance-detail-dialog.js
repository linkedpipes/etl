define([
  "jquery",
  "@client/app-service/jsonld/jsonld",
  "../detail-directive/instance-detail-directive",
  "@client/template/template-service"
], function (jQuery, jsonld, instanceDirective, templateService) {
  "use strict";

  const LP = {
    "template": "http://linkedpipes.com/ontology/template",
    "color": "http://linkedpipes.com/ontology/color",
    "configurationGraph": "http://linkedpipes.com/ontology/configurationGraph"
  };

  const SKOS = {
    "prefLabel": "http://www.w3.org/2004/02/skos/core#prefLabel",
    "note": "http://www.w3.org/2004/02/skos/core#note"
  };

  const DCTERMS = {
    "description": "http://purl.org/dc/terms/description"
  };

  // TODO Move to the separated module
  const i18 = {
    "str": function (value) {
      if (value === undefined) {
        return undefined;
      }
      if (Array.isArray(value)) {
        const result = [];
        value.forEach((item) => {
          result.push(item["@value"]);
        });
        return result;
      } else if (value["@value"] !== undefined) {
        return value["@value"];
      } else {
        return value;
      }
    }
  };

  function controller($scope, $mdDialog, templateService,
                      component, template, configuration) {
    $scope.api = {};

    $scope.onSave = () => {
      const newComponent = jQuery.extend(true, {}, component);

      // Update shared data with dialogs.
      // TODO: Rename to indicate that we are saving values from dialogs.
      $scope.api.save();

      // Save changes in instance.
      jsonld.r.setStrings(newComponent, SKOS.prefLabel,
        $scope.componentToEdit.label);
      if ($scope.componentToEdit.description === undefined ||
        $scope.componentToEdit.description === "") {
        jsonld.r.setStrings(newComponent, DCTERMS.description,
          undefined);
      } else {
        jsonld.r.setStrings(newComponent, DCTERMS.description,
          $scope.componentToEdit.description);
      }
      if ($scope.componentToEdit.color === undefined) {
        delete newComponent[LP.color];
      } else {
        newComponent[LP.color] = $scope.componentToEdit.color;
      }
      jsonld.r.setStrings(newComponent, SKOS.note,
          $scope.componentToEdit.note);
      $mdDialog.hide({
        "saved": true,
        "component": newComponent,
        "configuration": $scope.configuration
      });
    };

    $scope.onCancel = function () {
      $mdDialog.hide({"saved": false});
    };

    $scope.$on("$routeChangeStart", function ($event, next, current) {
      $mdDialog.cancel();
    });

    function initDirective() {

      if (template.supportControl) {
        $scope.componentToEdit.useTemplateConfig = false;
      } else {
        // Load from configuration.
        $scope.componentToEdit.useTemplateConfig = false;
      }

      $scope.api.store = {
        "instance": $scope.componentToEdit,
        "parent": template,
        "configuration": $scope.configuration
      };

      if ($scope.api.load !== undefined) {
        $scope.api.load();
      }

    }

    function init() {

      $scope.componentToEdit = {
        "id": jsonld.r.getId(component),
        "label": i18.str(jsonld.r.getString(
          component, SKOS.prefLabel)),
        "description": i18.str(jsonld.r.getString(
          component, DCTERMS.description)),
        "color": i18.str(jsonld.r.getString(
          component, LP.color)),
        "note": i18.str(jsonld.r.getString(
            component, SKOS.note))
      };

      $scope.infoLink = template._coreReference.infoLink;

      const configIRI = jsonld.r.getIRI(component,
        "http://linkedpipes.com/ontology/configurationGraph");
      if (configIRI === undefined) {
        // TODO Should this be happening here?
        templateService.fetchNewConfig(template.id).then((config) => {
          $scope.configuration = jQuery.extend(true, [], config);
          initDirective();
        });
      } else {
        $scope.configuration = jQuery.extend(true, [], configuration);
        initDirective();
      }
    }

    templateService.load().then(init);

  }

  let _initialized = false;
  return function init(app) {
    if (_initialized) {
      return;
    }
    _initialized = true;
    //
    instanceDirective(app);
    templateService(app);
    //
    app.controller("instance.detail.dialog", [
      "$scope", "$mdDialog", "template.service",
      "component", "component-template", "configuration",
      controller]);
  };

});
