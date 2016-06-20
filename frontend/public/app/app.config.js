define([], function () {
    function config($routeProvider, $mdIconProvider, $mdThemingProvider) {
        $routeProvider
                .when('/executions', {
                    'templateUrl': 'app/components/executions/listView/executionListView.html',
                    'controller': 'components.executions.list',
                    'activeView': 'executions',
                    'pageTitle': 'Executions - LinkedPipes ETL',
                    'color': '#FF9800'
                })
                .when('/executions/detail', {
                    'templateUrl': 'app/components/executions/detailView/executionDetailView.html',
                    'controller': 'components.executions.detail',
                    'activeView': 'executions',
                    'pageTitle': 'Execution - LinkedPipes ETL',
                    'color': '#FF9800'
                })
                .when('/pipelines', {
                    'templateUrl': 'app/components/pipelines/listView/pipelineListView.html',
                    'controller': 'components.pipelines.list',
                    'activeView': 'pipelines',
                    'pageTitle': 'Pipelines - LinkedPipes ETL',
                    'color': '#2196F3'
                })
                .when('/pipelines/upload', {
                    'templateUrl': 'app/components/pipelines/uploadView/pipelineUploadView.html',
                    'controller': 'components.pipelines.upload',
                    'activeView': 'pipelines',
                    'pageTitle': 'Pipelines - LinkedPipes ETL',
                    'color': '#2196F3'
                })
                .when('/pipelines/edit/canvas', {
                    'templateUrl': 'app/components/pipelines/canvasView/pipelineCanvasView.html',
                    'controller': 'components.pipeline.canvas.view',
                    'activeView': 'pipelines',
                    'pageTitle': 'Pipeline - LinkedPipes ETL'
                })
                .otherwise({
                    'redirectTo': '/executions'
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
                .icon('close', 'libraries/google.design/ic_close_24px.svg')
                .icon('copy', 'libraries/google.design/ic_content_copy_24px.svg')
                .icon('timeline', 'libraries/google.design/ic_timeline_24px.svg')
                .icon('more_vert', 'libraries/google.design/ic_more_vert_24px.svg')
                .icon('visibility', 'libraries/google.design/ic_visibility_24px.svg')
                .icon('keyboard_tab', 'libraries/google.design/ic_keyboard_tab_24px.svg')
                .icon('autorenew', 'libraries/google.design/ic_autorenew_24px.svg')
                .icon('clear', 'libraries/google.design/ic_clear_48px.svg')
                .icon('transform', 'libraries/google.design/ic_transform_24px.svg')
                .icon('call_split', 'libraries/google.design/ic_call_split_24px.svg')
                .icon('save', 'libraries/google.design/ic_save_24px.svg')
                .icon('settings', 'libraries/google.design/ic_settings_24px.svg')
                .icon('help_outline', 'libraries/google.design/ic_help_outline_24px.svg')
                .icon('delete_forever', 'libraries/google.design/ic_delete_forever_24px.svg')
                .icon('file_download', 'libraries/google.design/ic_file_download_24px.svg')
                .icon('ic_content_paste', 'libraries/google.design/ic_content_paste_24px.svg');

        $mdThemingProvider.theme('default')
                .primaryPalette('blue')
                .accentPalette('orange');

    }
    // Inject dependency.
    config.$inject = ['$routeProvider', '$mdIconProvider', '$mdThemingProvider'];
    return config;
});
