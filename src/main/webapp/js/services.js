/**
 * Created by Atheesan on 3/02/14.
 */


angular.module('spaceServices', ['ngResource'])
    .factory('Login', function ($resource) {
        return $resource('/api/accesstokens')
    })
    .factory('Register', function ($resource) {
        return $resource('/api/user')
    })
    .factory('Profile', function ($resource) {
        return $resource('/api/auth/user')
    })
    .factory('Contact', function ($resource) {
        return $resource('/api/auth/profile')
    })
    .factory('Spinner', function () {
        var opts = {
            lines: 13, // The number of lines to draw
            length: 20, // The length of each line
            width: 10, // The line thickness
            radius: 30, // The radius of the inner circle
            corners: 1, // Corner roundness (0..1)
            rotate: 0, // The rotation offset
            direction: 1, // 1: clockwise, -1: counterclockwise
            color: '#000', // #rgb or #rrggbb or array of colors
            speed: 1, // Rounds per second
            trail: 60, // Afterglow percentage
            shadow: false, // Whether to render a shadow
            hwaccel: false, // Whether to use hardware acceleration
            className: 'spinner', // The CSS class to assign to the spinner
            zIndex: 2e9, // The z-index (defaults to 2000000000)
            top: 'auto', // Top position relative to parent in px
            left: 'auto' // Left position relative to parent in px
        };
        var target = document.getElementById('mainContainer');
        var spinner = new Spinner(opts);
        return {"spinner": spinner, "target": target};
    }
)
    .factory('UserService',function () {
        return {
            username: '',
            email: '',
            password: '',
            accessToken: null,
            loggedIn: false
        };
    }).factory('Map',function ($resource) {
        return $resource('/api/map')
    }).factory('Game',function ($resource) {
        return $resource('/api/auth/game')
    }).factory('Action', function ($resource) {
        return $resource('/api/auth/action')
    })
    .factory('ActiveGame', function ($resource) {
        return $resource('/api/auth/game/specificGame/:gameId', {gameId: '@GameId'})
    })
    .factory('GameInvite',function ($resource){
        return $resource('/api/auth/game/invite/:gameId',{gameId: '@gameId'})
    })
//    .factory('FindPlayer', function ($resource) {
//        return $resource('/api/auth/findusersbyusername/:username', {username: '@username'},{'get': { method: 'GET', params: {username: '@username'}, format: 'json', isArray: true }})
//    })
    .factory('FindPlayer', function($resource) {

        var factory = {};

        factory.findUsersByUsername = function() {
            return $resource('/api/auth/findusersbyusername/:username', {username: '@username'},{'get': { method: 'GET', params: {username: '@username'}, format: 'json', isArray: true }})
        };

        factory.findUsersByEmail = function() {
            return $resource('/api/auth/findusersbyemail?email=:email', {username: '@email'},{'get': { method: 'GET', params: {username: '@email'}, format: 'json', isArray: true }})
        };

        factory.findUserByUserId = function() {
            return $resource('/api/auth/findUserByUserId/:userId', {userId: '@userId'},{'get': { method: 'GET', params: {userId: '@userId'}, format: 'json', isArray: false }})
        };



        return factory;

    }).factory('AuthorizationService', function($resource){
        return $resource('/api/auth/accesstokens');
    }).factory('ReplayGame', function($resource) {
        var factory = {};
        factory.getRevisionsByGameId = function() {
            return $resource('/api/auth/replay/:gameId', {gameId: '@gameId'},{'get': { method: 'GET', params: {gameId: '@gameId'}, format: 'json', isArray: false }})
        };
        factory.getRevisionByRevisionNumber = function() {
            return $resource('/api/auth/replay/:gameId/:revisionNumber', {gameId:'@gameId', revisionNumber: '@revisionNumber'},{'get': { method: 'GET', params: {gameId: '@gameId', revisionNumber:'@revisionNumber'}, format: 'json', isArray: false }})
        };
        return factory;
    }).factory('StatisticsService', function($resource){
        return $resource('/api/auth/statistics');
    }).factory('MailService', function($resource){
        return $resource('/api/auth/invitation');
    }).factory('VerificationService', function($resource){
        return $resource('/api/verifiedUser');
    }).factory('RegisterFbUser', function($resource){
        return $resource('/api/fbuser')
    });
