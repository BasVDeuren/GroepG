/**
 * Created by Dimi on 3/02/14.
 */

var spaceApp = angular.module('spaceApp', ['ngRoute', 'spaceServices', 'ngCookies', 'ngAnimate', 'pascalprecht.translate', 'ui.bootstrap', 'imageupload', 'firebase', 'mgcrea.ngStrap', 'angular-md5'])
    .config(appRouter);

//Navigation
function appRouter($routeProvider, $httpProvider) {

    var interceptor = ['$rootScope', '$q', '$location', function ($rootScope, $q, $location) {
        function success(response) {
            if (response.config.url.indexOf("/api/auth") > -1) {
                $rootScope.loggedInBool = true;
                $rootScope.$apply();
            }
            return response;
        }

        function error(response) {
            var status = response.status;
            if ($location.path() !== "/login" && $location.path() !== "/verify") {
                if (status == 401) {
                    $rootScope.loggedInBool = false;
                    $rootScope.facebookBool = false;
                    $rootScope.$apply();
                    console.info("unauthorized");
                    console.info($location.path());
                    console.info("back to loginpage");
                    $location.path("/login");

                    return;
                }
            }
            // otherwise
            return $q.reject(response);

        }

        return function (promise) {
            return promise.then(success, error);
        }

    }];
    $httpProvider.responseInterceptors.push(interceptor);

    $routeProvider
        .when('/', {
            templateUrl: 'partials/navhome.html',
            controller: 'NavHomeController'
        })
        .when('/login', {
            templateUrl: 'partials/login.html',
            controller: 'LoginController'
        }).when('/verify', {
            templateUrl: 'partials/verify.html',
            controller: 'VerifyController'
        })
        .when('/spacecrack/game', {
            templateUrl: 'partials/game.html',
            controller: 'GameController'
        })
        .when('/spacecrack/register', {
            templateUrl: 'partials/register.html',
            controller: 'RegisterController'
        })
        .when('/spacecrack/editProfile', {
            templateUrl: 'partials/editProfile.html',
            controller: 'ProfileController'
        })
        .when('/spacecrack/home', {
            templateUrl: 'partials/navhome.html',
            controller: 'NavHomeController'
        })
        .when('/spacecrack/game/:gameId', {
            templateUrl: 'partials/game.html',
            controller: 'GameController'
        })
        .when('/spacecrack/activegames', {
            templateUrl: 'partials/activeGames.html',
            controller: 'ActiveGamesController'
        })
        .when('/help', {
            templateUrl: 'partials/help.html'
        })
        .when('/notFound', {
            templateUrl: 'partials/notFound.html'
        })
        .when('/spacecrack/createGame', {
            templateUrl: 'partials/createGame.html',
            controller: 'createGameController'
        }).when('/spacecrack/chat', {
            templateUrl: 'partials/chat.html',
            controller: 'ChatController'
        }).when('/spacecrack/oldgame/:gameId', {
            templateUrl: 'partials/oldgame.html',
            controller: 'ReplayGameController'
        }).when('/spacecrack/matchhistory', {
            templateUrl: 'partials/matchHistory.html',
            controller: 'ActiveGamesController'
        }).when('/spacecrack/statistics', {
            templateUrl: 'partials/statistics.html',
            controller: 'StatisticsController'
        });

    $routeProvider.otherwise({redirectTo: '/notFound'});
}

//Translation
spaceApp.config(['$translateProvider', function ($translateProvider) {
    $translateProvider.useStaticFilesLoader({
        prefix: 'json/languages/',
        suffix: '.json'
    });
    $translateProvider.preferredLanguage('en_US');
}]);

spaceApp.controller("MainController", function ($scope, $cookies, $location, $timeout, $translate, $cookieStore, Login, $rootScope, $firebase, Profile, GameInvite) {
    var loginData = {
        password: ""
    };

    $scope.changeLanguage = function (key) {
        $translate.uses(key);
    };
    //site locatie wijzigen
    $scope.go = function (path) {
        $location.path(path);
    };

    $scope.logout = function () {
        Login.delete(function () {
            $rootScope.loggedInBool = false;
            $rootScope.facebookBool = false;
            $cookieStore.remove('accessToken');
            $scope.go('/login');
        }, function () {

        });

    };

    //Invite Notifications

    $rootScope.invitesArray = [];
    $rootScope.messagesArray = [];
    $rootScope.userInvites = [];
    $rootScope.userMessages = [];

    $rootScope.loadInvites = function () {
        $rootScope.invitesArray = [];
        Profile.get(function (data) {
            //invites
            var firebaseUrl = 'https://vivid-fire-9476.firebaseio.com/invites/' + data.username.replace(/ /g, '') + data.profile.profileId;
            var ref = new Firebase(firebaseUrl);
            $rootScope.userInvites = $firebase(ref);

            ref.on("child_added", function (dataSnapshot) {
                if (!dataSnapshot.val().read) {
                    if (dataSnapshot.val().invitedId == data.profile.profileId && dataSnapshot.val().invited == data.username) {
                        $rootScope.invitesArray.push(dataSnapshot.val());
                    }
                }
            });

            //messages
            var firebaseMsgUrl = 'https://vivid-fire-9476.firebaseio.com/messages/' + data.username.replace(/ /g, '') + data.profile.profileId;
            var refMsg = new Firebase(firebaseMsgUrl);
            $rootScope.userMessages = $firebase(refMsg);

            refMsg.on("child_added", function (dataSnapshot) {
                if (!dataSnapshot.val().read) {
                    $rootScope.messagesArray.push(dataSnapshot.val());
                }
            });

        }, function (data, headers) {
        });
    };
    $rootScope.loadInvites();

    $rootScope.acceptInvite = function (game) {
        GameInvite.save({gameId: game.gameId}, function () {
            var keys = $rootScope.userInvites.$getIndex();
            keys.forEach(function (key, i) {
                if ($rootScope.userInvites[key] == game) {
                    $rootScope.userInvites[key].accepted = true;
                    $rootScope.userInvites.$save(key);
                }
            });
            $rootScope.invitesArray = [];
            var firebaseUrl = 'https://vivid-fire-9476.firebaseio.com/messages/' + game.inviter + game.inviterId;
            var ref = new Firebase(firebaseUrl);
            $scope.allMessages = $firebase(ref);
            $scope.allMessages.$add({sender: game.invited, senderId: game.invitedId,receiver: game.inviter,receiverId:game.inviterId, read: false, gameId: game.gameId});
            $scope.go('/spacecrack/game/' + game.gameId);
            $rootScope.loadInvites();
        });
    };

    $scope.checkUnreadInvites = function () {
        if ($rootScope.invitesArray.length == 0) {
            return false;
        }
        return true;
    };

    $scope.checkUnreadMessages = function () {
        if ($rootScope.messagesArray.length == 0) {
            return false;
        }
        return true;
    };

    $scope.setInvitesAsRead = function () {
        var keys = $rootScope.userInvites.$getIndex();
        keys.forEach(function (key, i) {
            $rootScope.userInvites[key].read = true;
            $rootScope.userInvites.$save(key);
        });

        $rootScope.invitesArray = [];
    };

    $scope.setMessagedAsRead = function () {
        var keys = $rootScope.userMessages.$getIndex();
        keys.forEach(function (key, i) {
            $rootScope.userMessages[key].read = true;
            $rootScope.userMessages.$save(key);
        });

        $rootScope.messagesArray = [];
    };

    Profile.get(function (data) {
        console.log("get api/auth/user");
        loginData.password = data.password;
        if (loginData.password.substr(0, 8) == ("facebook")) {
            $rootScope.facebookBool = true;
        } else {
            $rootScope.facebookBool = false;
        }
    }, function (data) {
        console.log("WRONG");
    });


});

(function (d, s, id) {
    var js, fjs = d.getElementsByTagName(s)[0];
    if (d.getElementById(id)) return;
    js = d.createElement(s);
    js.id = id;
    js.src = "//connect.facebook.net/nl_NL/all.js#xfbml=1";
    fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));




