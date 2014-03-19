/**
 * Created by Atheesan on 4/02/14.
 */
var spaceApp = angular.module('spaceApp');

spaceApp.controller("RegisterController", function ($scope, Register, $cookieStore, Spinner,$rootScope,md5) {
        $scope.registerData = {
        email: "",
            username: "",
            password: "",
            passwordRepeated: ""
    };


        $scope.hasRegistrationFailed = false;
    $scope.userNameAlreadyInUse = false;
        $scope.register = function () {
            var regUsername = $scope.registerData.username.replace(/ /g, '_');
            var hashedRegisterData = {
            email: $scope.registerData.email,
            username: regUsername,
            password: md5.createHash($scope.registerData.password),
            passwordRepeated: md5.createHash($scope.registerData.passwordRepeated)
        };

            Spinner.spinner.spin(Spinner.target);
            Register.save(hashedRegisterData, function () {
                Spinner.spinner.stop();
                $scope.go('/verify');
                $scope.hasRegistrationFailed = false;
                $scope.userNameAlreadyInUse = false;
            }, function (response) {
                Spinner.spinner.stop();
                if(response.status !== 409)
                {
                    $scope.hasRegistrationFailed = true;
                }
                $scope.userNameOrEmailAlreadyInUse = true;

            });
        };

        $scope.checkPassword = function (password1, password2) {
            return password1 == password2;
        };

        $scope.validateRegister = function () {
            if ($scope.registerData.email != '' && $scope.registerData.username != '' && $scope.registerData.password != '' && $scope.registerData.passwordRepeated != ''
                && $scope.checkPassword($scope.registerData.password, $scope.registerData.passwordRepeated)) {
                return false;
            } else {
                return true;
            }
        };


//    $scope.fbRegister = function() {
//        FB.login(function(response) {
//            if (response.authResponse) {
//                var user;
//                FB.api('/me', function(response) {
//                    user = {
//                        email: response.email,
//                        username: response.name,
//                        password: 'facebook' + response.id,
//                        passwordRepeated: 'facebook' + response.id
//                    };
//
//                    Register.save(user, function (data, headers) {
//                        $cookieStore.put('accessToken',data.value);
//                        $scope.go('/spacecrack/home');
//                        $scope.hasRegistrationFailed = false;
//                    }, function (data, headers) {
//                        $scope.hasRegistrationFailed = true;
//                    });
//                });
//
//            } else {
//                console.log('User cancelled login or did not fully authorize.');
//            }
//        }, {scope: 'email'});
//    }

});
