//
// Application entry point.
//

'use strict';

var gExpress = require('express');
var gApp = gExpress();

// Load configuration.
var gConfiguration = require('./modules/configuration');

// Support for POST content.
var gBodyParser = require('body-parser');
gApp.use(gBodyParser.urlencoded({extended: true}));
gApp.use(gBodyParser.json());

// Static content.
gApp.use('/app', gExpress.static('public/app'));
gApp.use('/libraries', gExpress.static('public/libraries'));
gApp.use('/assets', gExpress.static('public/assets'));

// REST API.
gApp.use('/api/v1/', require('./routes/api'));
gApp.use('/resources/', require('./routes/resources'));

// Main page and rest of angular application.
gApp.engine('html', require('ejs').renderFile);
gApp.set('views', __dirname + '/public/');
gApp.get('/', function (req, res) {
    res.render('index.html');
});

// Start server.
var server = gApp.listen(gConfiguration.frontend.port, function () {
    console.log('We have started our server on port ', gConfiguration.frontend.port);
});

// Add event handlers.
process.on('SIGTERM', function () {
    // Finish current requests.
    server.close(function () {
        console.log('Closing server on "SIGTERM".');
        process.exit(0);
    });
});

process.on('SIGHUP', function (code) {
    console.log('Closing server on "SIGHUP".');
    process.exit(0);
});

process.on('SIGINT', function (code) {
    console.log('Closing server on "SIGINT".');
    process.exit(0);
});

process.on('exit', function (code) {
    console.log('About to exit with code:', code);
});

process.on('uncaughtException', function (err) {
    console.log('Caught exception:', err, err.stack);
});

