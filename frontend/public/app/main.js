requirejs.config({
    baseUrl: '',
    paths: {
        'jquery': 'libraries/jquery/jquery.min',
        // Angular.
        'angular': 'libraries/angularjs/angular.min',
        'angular-route': 'libraries/angularjs/angular-route.min',
        'angular-resource': 'libraries/angularjs/angular-resource.min',
        'angular-messages': 'libraries/angularjs/angular-messages.min',
        'angular-cookies': 'libraries/angularjs/angular-cookies.min',
        // Angular Material and dependencies.
        // https://github.com/angular/material/tree/v1.0.4
        // http://stackoverflow.com/questions/27567822/angular-material-with-requirejs
        // http://stackoverflow.com/questions/29542275/angular-material-requirejs-not-working-together
        'angular-aria': 'libraries/angularjs/angular-aria.min',
        'angular-animate': 'libraries/angularjs/angular-animate.min',
        'angular-material': 'libraries/angularjs.material/angular-material.min',
        //
        'angular-file-upload': 'libraries/ng-file-upload/ng-file-upload.min',
        // https://github.com/alexcrack/angular-ui-notification
        'angular-ui-notification': 'libraries/ui-notification/angular-ui-notification.min',
        //
        'hammerjs': 'libraries/hammerjs/hammer.min',
        // JoinJS and dependencies.
        'jointjs': 'libraries/jointjs/joint.core.min',
        'lodash': 'libraries/lodash/lodash.min',
        'backbone': 'libraries/backbone/backbone.min',
        // Library for client side download
        'file-saver': 'libraries/filesaver/FileSaver.min',
        //
        'angular-clipboard' : 'libraries/angular-clipboard/angular-clipboard',
        // Custom modules.
        "jsonld": "/app/modules/jsonld",
        "lp-vocabulary": "/app/modules/lp-vocabulary"
    },
    map: {
        'backbone': {
            // Backbone requires underscore, but we have lodas instead. So we replace 'underscore' with 'lodash'.
            'underscore': 'lodash'
        }
    },
    shim: {
        'angular': {// Angular does not support AMD out of the box, put it in a shim.
            deps: [
                'jquery'
            ],
            exports: 'angular'
        },
        'angular-route': {
            deps: ['angular']
        },
        'angular-resource': {
            deps: ['angular']
        },
        'angular-messages': {
            deps: ['angular']
        },
        'angular-cookies': {
            deps: ['angular']
        },
        'angular-animate': {
            deps: ['angular']
        },
        'angular-aria': {
            deps: ['angular']
        },
        'angular-material': {
            deps: ['angular-animate', 'angular-aria']
        },
        'angular-file-upload': {
            deps: ['angular']
        },
        'angular-ui-notification': {
            deps: ['angular']
        },
        'bootstrap-ui': {
            deps: ['angular']
        }
    }
});

requirejs(['app/app.module'], function (app) {
    app.bootstrap();
});