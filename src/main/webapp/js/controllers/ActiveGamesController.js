var spaceApp = angular.module('spaceApp');
spaceApp.controller("ActiveGamesController", function ($scope, $translate, Game, Profile) {

    $scope.games = [
    {gameId:""}];

    Game.query(function(data){
        alert("hallo success!!!");
        $scope.games = data;
    }, function(){
        alert("FAIL!!!");
    });

});