define([
  "./about/about-ctrl.js",
  "./personalization/personalization-ctrl",
  "./pipeline/list/pipeline-list-ctrl",
  "./execution/list-view/execution-list-ctrl",
  "./template/list/template-list-view",
  "./pipeline/upload/pipeline-upload-ctrl",
  "./pipeline/edit/pipeline-edit-ctrl",
  "./template/detail/template-detail-ctrl"
], function (
  _about, _personalization, _pipelineList, _executionList, _templateList,
  _pipelineUpload, _pipelineEdit, _templateDetail) {

  function configureNavigation($routeProvider) {
    $routeProvider
      .when("/help", {
        "template": require("./about/about-view.html"),
        "controller": "view-about",
        "activeView": "about",
        "pageTitle": "Help",
        "color": "#999999"
      })
      .when("/personalization", {
        "template": require("./personalization/personalization-view.html"),
        "controller": "view-personalization",
        "activeView": "personalization",
        "pageTitle": "Personalization",
        "color": "#999999"
      })
      .when("/executions", {
        "template": require("./execution/list-view/execution-list-view.html"),
        "controller": "components.executions.list",
        "activeView": "executions",
        "pageTitle": "Executions",
        "color": "#FF9800"
      })
      .when("/templates", {
        "template": require("./template/list/template-list-view.html"),
        "controller": "template.list",
        "activeView": "templates",
        "pageTitle": "Templates",
        "color": "#2196F3"
      })
      .when("/templates/detail", {
        "template": require("./template/detail/template-detail-view.html"),
        "controller": "template.detail.view",
        "activeView": "templates",
        "pageTitle": "Templates",
        "color": "#2196F3"
      })
      .when("/pipelines", {
        "template": require("./pipeline/list/pipeline-list-view.html"),
        "controller": "view-pipelines-list",
        "activeView": "pipelines",
        "pageTitle": "Pipelines",
        "color": "#2196F3"
      })
      .when("/pipelines/upload", {
        "template": require("./pipeline/upload/pipeline-upload-view.html"),
        "controller": "components.pipelines.upload",
        "activeView": "pipelines",
        "pageTitle": "Pipelines",
        "color": "#2196F3"
      })
      .when("/pipelines/edit/canvas", {
        "template": require("./pipeline/edit/pipeline-edit-view.html"),
        "controller": "components.pipeline.canvas.view",
        "activeView": "pipelines",
        "pageTitle": "Pipeline"
      })
      .otherwise({
        "redirectTo": "/executions"
      });

  }

  return (app) => {
    _about(app);
    _personalization(app);
    _pipelineList(app);
    _executionList(app);
    _templateList(app);
    _pipelineUpload(app);
    _pipelineEdit(app);
    _templateDetail(app);
    app.config(["$routeProvider", configureNavigation])
  };
});

// TODO Move all navigation here to a single place (ie. calls of $location.path).
