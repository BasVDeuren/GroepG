/**
 * Created by Atheesan on 4/02/14.
 */
function LoginController($scope, Login, $cookieStore, Spinner, Contact,$rootScope,md5,RegisterFbUser) {

    //data klaar zetten
    $scope.contactData = {
        firstname: "",
        lastname: "",
        dayOfBirth: "",
        image: "",
        email: ""
    };

    Contact.get(function (data) {
        $scope.contactData.firstname = data.firstname;
        $scope.contactData.lastname = data.lastname;
        $scope.convertedDate.value = new Date(data.dayOfBirth);
        $scope.contactData.image = data.image;
        $scope.contactData.email = data.email;
    }, function () {
    });

    //Loading Spinner
    $scope.startLoading = false;
    $scope.loginData = {
        email: "",
        password: ""
    };
    $scope.hasLoginFailed = false;
    $scope.alreadyRegistered = false;
    $scope.login = function () {
        Spinner.spinner.spin(Spinner.target);
        $.blockUI({ message: null });

        var hashedLoginData = {
            email: $scope.loginData.email,
            password: md5.createHash($scope.loginData.password)
        };
        Login.save(hashedLoginData, function (data) {
            Spinner.spinner.stop();
            $.unblockUI();
            $cookieStore.put('accessToken', data.value);
            $scope.go('/');
            $scope.hasLoginFailed = false;
        }, function () {
            Spinner.spinner.stop();
            $.unblockUI();
            $scope.hasLoginFailed = true;
        });

    };

    $scope.validateLogin = function () {
        return !($scope.loginData.email != '' && $scope.loginData.password != '');
    };

    $scope.fbLogin = function () {
        FB.login(function (response) {
            if (response.authResponse) {
                var user;
                console.log(response);
                FB.api('/me', function (response) {
                    console.log(response);
                    $scope.contactData.dayOfBirth = new Date(response.birthday).toLocaleDateString();
                    FB.api("/me/picture", function (response) {
                        console.log(response);
                        $scope.contactData.image = response.data.url;
                    });
                    user = {
                        email: response.email,
                        password: 'facebook' + response.id
                    };
                    Spinner.spinner.spin(Spinner.target);
                    Login.save(user, function (data) {
                        Spinner.spinner.stop();
                        $cookieStore.put('accessToken', data.value);
                        $scope.updateFbProfile(response);
                        $rootScope.loadInvites();
                        $scope.go('/');
                        $scope.hasLoginFailed = false;
                    }, function () {
                        Spinner.spinner.stop();
                        $scope.registerFB(response);
                    });
                });
            } else {
//                    Spinner.spinner.stop();
                console.log('User cancelled login or did not fully authorize.');
            }
        }, {scope: 'email, user_birthday, user_photos, read_friendlists'});
    };

    $scope.registerFB = function (response) {
        Spinner.spinner.spin(Spinner.target);
        var fbUsername = response.name.replace(/ /g, '_');
        var user = {
            email: response.email,
            username: fbUsername,
            password: 'facebook' + response.id,
            passwordRepeated: 'facebook' + response.id
        };

        RegisterFbUser.save(user, function (data) {
            Spinner.spinner.stop();
            $cookieStore.put('accessToken', data.value);
            $scope.updateFbProfile(response);
            $rootScope.loadInvites();
           // $scope.go('/');
            $scope.fbLogin();
            $scope.alreadyRegistered = false;
        }, function () {
            Spinner.spinner.stop();
            $scope.alreadyRegistered = true;
        });
    };

    //Update profile with facebook data

    $scope.updateFbProfile = function (response) {

        $scope.contactData.firstname = response.first_name;
        $scope.contactData.lastname = response.last_name;
        $scope.contactData.email = response.email;
        Contact.save($scope.contactData, function () {
        }, function () {
        })
    }
}