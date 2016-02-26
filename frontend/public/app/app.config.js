define([], function () {
    function config($routeProvider, $mdIconProvider, $mdThemingProvider) {
        $routeProvider
                .when('/executions', {
                    templateUrl: 'app/components/executions/listView/executionListView.html',
                    controller: 'components.executions.list',
                    activeView: 'executions'
                })
                .when('/executions/detail', {
                    templateUrl: 'app/components/executions/detailView/executionDetailView.html',
                    controller: 'components.executions.detail',
                    activeView: 'executions'
                })
                .when('/pipelines', {
                    templateUrl: 'app/components/pipelines/listView/pipelineListView.html',
                    controller: 'components.pipelines.list',
                    activeView: 'pipelines'
                })
                .when('/pipelines/upload', {
                    templateUrl: 'app/components/pipelines/uploadView/pipelineUploadView.html',
                    controller: 'components.pipelines.upload',
                    activeView: 'pipelines'
                })
                .when('/pipelines/edit/canvas', {
                    templateUrl: 'app/components/pipelines/canvasView/pipelineEditCanvasView.html',
                    controller: 'components.pipelines.edit.canvas',
                    activeView: 'pipelines'
                })
                .otherwise({
                    redirectTo: '/executions'
                });
        //
        $mdIconProvider
                .icon('add', 'libraries/google.design/ic_add_48px.svg')
                .icon('add_circle_outline', 'libraries/google.design/ic_add_circle_outline_48px.svg')
                .icon('file_upload', 'libraries/google.design/ic_file_upload_48px.svg')
                .icon('menu', 'libraries/google.design/ic_menu_48px.svg')
                .icon('play_circle_outline', 'libraries/google.design/ic_play_circle_outline_48px.svg')
                .icon('done', 'libraries/google.design/ic_done_48px.svg')
                .icon('error', 'libraries/google.design/ic_error_outline_48px.svg')
                .icon('edit', 'libraries/google.design/ic_mode_edit_48px.svg')
                .icon('delete', 'libraries/google.design/ic_delete_48px.svg')
                .icon('run', 'libraries/google.design/ic_directions_run_48px.svg')
                .icon('hourglass', 'libraries/google.design/ic_hourglass_empty_48px.svg')
                .icon('info', 'libraries/google.design/ic_info_outline_48px.svg')
                .icon('help', 'libraries/google.design/ic_help_outline_48px.svg')
                .icon('close_small', 'libraries/google.design/ic_close_24px.svg')
                .icon('copy', 'libraries/google.design/ic_content_copy_24px.svg')
                .icon('timeline', 'libraries/google.design/ic_timeline_24px.svg')
                .icon('more_vert', 'libraries/google.design/ic_more_vert_24px.svg')
                .icon('visibility', 'libraries/google.design/ic_visibility_24px.svg')
                .icon('keyboard_tab', 'libraries/google.design/ic_keyboard_tab_24px.svg')
                .icon('autorenew', 'libraries/google.design/ic_autorenew_24px.svg')
                .icon('file_download', 'libraries/google.design/ic_file_download_24px.svg');

        $mdThemingProvider.theme('default')
                .primaryPalette('blue')
                .accentPalette('orange');

    }
    // Inject dependency.
    config.$inject = ['$routeProvider', '$mdIconProvider', '$mdThemingProvider'];
    return config;
});
