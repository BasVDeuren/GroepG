/**
 * Created by Tim on 18/03/14.
 */
var spaceApp = angular.module('spaceApp');
spaceApp.controller("VerifyController", function ($scope, $translate,$timeout, VerificationService) {
  $scope.verified = "verifying";
    $scope.verificationToken = "";
    $scope.invalidToken = false;
    $scope.verify = function(){
        VerificationService.save({tokenValue: $scope.verificationToken}, function(){
            $scope.verified= "success";
            $scope.invalidToken = false;
            $timeout(function(){
                $scope.go('/login');
            }, 2000);
        }, function(){
            $scope.invalidToken = true;
        });
    }

});