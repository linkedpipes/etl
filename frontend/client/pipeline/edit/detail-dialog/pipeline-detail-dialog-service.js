((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "@client/app-service/vocabulary",
      "@client/app-service/jsonld/jsonld",
      "@client/app-service/design"
    ], definition);
  }
})((vocabulary, jsonld, _pipelineDesignService) => {
  const LP = vocabulary.LP;
  const SKOS = vocabulary.SKOS;

  // TODO Replace jsonldService with jsonld module.
  function factory(pipelineService) {

    let $scope;
    let model;

    function initialize(_scope, _model) {
      $scope = _scope;
      model = _model;

      $scope.tags = {
        "searchText": "",
        "all": [],
        "querySearch": tagQuerySearch
      };

      $scope.detail = createScopeDetail(model);
      $scope.profile = createScopeProfile(model);

      // TODO Made clear that this returns all tags in the instance.
      $scope.tags.all = pipelineService.getTags();
    }

    function tagQuerySearch(query) {
      query = query.toLowerCase();
      return $scope.tags.all.filter((item) => {
        return item.toLowerCase().indexOf(query) !== -1;
      });
    }

    function cancel($mdDialog) {
      $mdDialog.hide();
    }

    function save($mdDialog) {
      saveDialogToPipeline();
      $mdDialog.hide();
    }

    function saveDialogToPipeline() {
      jsonld.r.setStrings(model.definition, SKOS.PREF_LABEL,
        $scope.detail.label);
      jsonld.r.setStrings(model.definition, LP.HAS_TAG,
        $scope.detail.tags);
      jsonld.r.setIRIs(model.profile, LP.HAS_REPO_POLICY,
        $scope.profile.rdfRepositoryPolicy);
      jsonld.r.setIRIs(model.profile, LP.HAS_REPO_TYPE,
        $scope.profile.rdfRepositoryType);
    }

    return {
      "initialize": initialize,
      "cancel": cancel,
      "save": save
    };
  }

  factory.$inject = [
    "service.pipelineDesign"
  ];

  function createScopeDetail(model) {
    return {
      "uri": model.definition["@id"],
      "label": jsonld.r.getPlainString(model.definition, SKOS.PREF_LABEL),
      "tags": jsonld.r.getPlainStrings(model.definition, LP.HAS_TAG)
    }
  }

  function createScopeProfile(model) {
    return {
      "rdfRepositoryPolicy":
        jsonld.r.getIRI(model.profile, LP.HAS_REPO_POLICY),
      "rdfRepositoryType":
        jsonld.r.getIRI(model.profile, LP.HAS_REPO_TYPE)
    };
  }

  let initialized = false;
  return function init(app) {
    if (initialized) {
      return;
    }
    initialized = true;
    _pipelineDesignService(app);
    app.factory("pipeline.detail.dialog.service", factory);
  }
});
