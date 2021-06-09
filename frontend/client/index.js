import angular from "angular";
import ngRoute from "angular-route";
import ngResource from "angular-resource";
import ngMaterial from "angular-material";
import ngMessages from "angular-messages";
import ngFileUpload from "ng-file-upload";
import ngCookies from "angular-cookies";
import "angular-clipboard";
import ngNotification from "angular-ui-notification";

import configureNavigation from "./navigation";
import appLayout from "./app-layout/layout-directive";

import "./style.css";
import "angular-material/angular-material.css";
import "angular-ui-notification/dist/angular-ui-notification.css";
import "jointjs/dist/joint.core.css";

const APP_NAME = "lp-application";

(function main() {
  const app = createApplication();
  appLayout(app);
  configurePathHashPrefix(app);
  configureNavigation(app);
  configureTheme(app);

  app.run(redirectFromHomePage);

  startApplication();
})();

function createApplication() {
  return angular.module(APP_NAME, [
    ngRoute,
    ngResource,
    ngMaterial,
    ngMessages,
    ngFileUpload,
    ngCookies,
    ngNotification,
    "angular-clipboard"
    // "lp-scroll-watch"
  ]);
}

function configurePathHashPrefix(app) {
  // https://stackoverflow.com/questions/41211875/angularjs-1-6-0-latest-now-routes-not-working
  app.config(["$locationProvider", ($locationProvider) => {
    $locationProvider.hashPrefix("");
  }]);
}

function configureTheme(app) {
  app.config(["$mdThemingProvider", ($mdThemingProvider) => {
    $mdThemingProvider.theme("default")
      .primaryPalette("blue")
      .accentPalette("orange");
  }]);
}

function redirectFromHomePage($rootScope, $route, $location, $cookies) {
  if ($location.path() === "") {
    let landingPage = $cookies.get("lp-landing");
    if (landingPage === undefined || landingPage === "") {
      landingPage = "/executions";
    }
    $location.path(landingPage);
  }
  preventReloadOnPathChange($rootScope, $route, $location);
}

function preventReloadOnPathChange($rootScope, $route, $location) {
  const originalPath = $location.path;
  $location.path = (path, reload) => {
    if (reload === false) {
      const lastRoute = $route.current;
      const un = $rootScope.$on("$locationChangeSuccess", () => {
        $route.current = lastRoute;
        un();
      });
    }
    return originalPath.apply($location, [path]);
  };
}

function startApplication() {
  angular.bootstrap(document, [APP_NAME]);
}