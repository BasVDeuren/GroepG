// Karma configuration
// Generated on Tue Feb 04 2014 23:19:33 GMT+0100 (Romance Standard Time)

module.exports = function (config) {
    config.set({

        // base path, that will be used to resolve files and exclude
        basePath: '',


        // frameworks to use
        frameworks: ['jasmine'],


        // list of files / patterns to load in the browser
        files: [
            'lib/angular/angular.js',
            'lib/angular/angular-*.js',
            'js/vendor/angular-translate-min.js',
            'js/vendor/phaser.min.js',
            'js/vendor/jquery-1.10.1.min.js',
            'js/vendor/bootstrap.min.js',
            'js/vendor/angular-translate-loader-static-files.min.js',
            'test/lib/angular/angular-mocks.js',
            'js/*.js',
            'js/controllers/*js',
            'test/unit/*.js',

        ],


        // list of files to exclude
        exclude: [
            'lib/angular/angular-loader.js',
            'lib/angular/*.min.js',
            'lib/angular/angular-scenario.js' ,
            'js/bootstrap-datepicker.js'
        ],


        // test results reporter to use
        // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
        reporters: ['progress'],


        // web server port
        port: 9876,


        // enable / disable colors in the output (reporters and logs)
        colors: true,


        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_INFO,


        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,


        // Start these browsers, currently available:
        // - Chrome
        // - ChromeCanary
        // - Firefox
        // - Opera (has to be installed with `npm install karma-opera-launcher`)
        // - Safari (only Mac; has to be installed with `npm install karma-safari-launcher`)
        // - PhantomJS
        // - IE (only Windows; has to be installed with `npm install karma-ie-launcher`)
        browsers: ['Chrome'],


        // If browser does not capture in given timeout [ms], kill it
        captureTimeout: 60000,


        // Continuous Integration mode
        // if true, it capture browsers, run tests and exit
        singleRun: false
    });
};
