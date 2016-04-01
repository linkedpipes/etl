requirejs.config({
    baseUrl: '',
    paths: {
        'jquery': 'libraries/jquery/2.2.0/jquery',
        // Angular.
        'angular': 'libraries/angularjs/1.4.1/angular',
        'angular-route': 'libraries/angularjs/1.4.1/angular-route',
        'angular-resource': 'libraries/angularjs/1.4.1/angular-resource',
        'angular-messages': 'libraries/angularjs/1.4.1/angular-messages',
        // Angular Material and dependencies.
        // https://github.com/angular/material/tree/v1.0.4
        // http://stackoverflow.com/questions/27567822/angular-material-with-requirejs
        // http://stackoverflow.com/questions/29542275/angular-material-requirejs-not-working-together
        'angular-aria': 'libraries/angularjs/1.4.1/angular-aria',
        'angular-animate': 'libraries/angularjs/1.4.1/angular-animate',
        'angular-material': 'libraries/angularjs.material/master/angular-material',
        //
        'angular-file-upload': 'libraries/ng-file-upload/11.2.3/ng-file-upload',
        // https://github.com/alexcrack/angular-ui-notification
        'angular-ui-notification': 'libraries/ui-notification/0.0.14/angular-ui-notification.min',
        //
        'hammerjs': 'libraries/hammerjs/2.0.6/hammer.min',
        // JoinJS and dependencies.
        'jointjs': 'app/components/pipelines/canvas/libraries/jointjs/0.9.7/joint',
        'lodash': '/app/components/pipelines/canvas/libraries/lodash/3.10.1/lodash.min',
        'backbone': '/app/components/pipelines/canvas/libraries/backbone/1.2.3/backbone',
        // Library for client side download
        'file-saver': 'libraries/filesaver/FileSaver'
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