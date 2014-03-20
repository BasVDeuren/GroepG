var spaceApp = angular.module('spaceApp');
spaceApp.controller("ActiveGamesController", function ($scope, $translate, $timeout, Game, $location, Contact, GameInvite) {

    $scope.oneAtATime = true;
    $scope.goToGame = function (gameId) {
        $scope.go('/spacecrack/game/' + gameId);
    };

    Contact.get(function (data) {
        $scope.currentProfileId = data.profileId;
    });


    $scope.games = [
        {gameId: "",
            name: "",
            loserPlayerId: ""
        }
    ];

    function bindGames() {
        Game.query(function (data) {
            $scope.games = data;
            $timeout(bindGames, 5000);
        });
    }
    bindGames();
    $scope.getPlayersOrdered = function (game) {
        var players = [];
        if (game.player1.profileId == $scope.currentProfileId) {
            players.push(game.player1);
            players.push(game.player2);
        } else {
            players.push(game.player2);
            players.push(game.player1);
        }

        return players;
    };

    $scope.isInvitedGame = function (game) {
        if ($scope.getPlayersOrdered(game)[0].requestAccepted == false && $scope.getPlayersOrdered(game)[1].requestAccepted == true) {
            return true;
        } else {
            return false;
        }
    };

    $scope.isActiveGame = function (game) {
        return ($scope.getPlayersOrdered(game)[0].requestAccepted && $scope.getPlayersOrdered(game)[1].requestAccepted);
    };

    $scope.isNotEndedGame = function (game) {
        if(game.loserPlayerId == 0){
            return true;
        }else{
            return false;
        }
    };

    $scope.isPendingGame = function (game) {
        if ($scope.getPlayersOrdered(game)[0].requestAccepted == true && $scope.getPlayersOrdered(game)[1].requestAccepted == false) {
            return true;
        } else {
            return false;
        }
    };

    $scope.acceptInvite = function (game) {
        GameInvite.save({gameId: game.gameId}, function () {
            $scope.go('/spacecrack/game/' + game.gameId);
        });
    };

    $scope.declineInvite = function (game) {
        GameInvite.delete({gameId: game.gameId}, function () {
            Game.query(function (data) {
                $scope.games = data;
            });
        });
    }

    $scope.cancelPendingGame = function (game) {
        GameInvite.delete({gameId: game.gameId}, function () {
            Game.query(function (data) {
                $scope.games = data;
            });
        });
    }

});