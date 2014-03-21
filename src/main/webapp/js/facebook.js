/**
 * Created with IntelliJ IDEA.
 * User: Dimi
 * Date: 11/02/14
 * Time: 15:29
 * To change this template use File | Settings | File Templates.
 */

window.fbAsyncInit = function() {
    FB.init({
        appId      : '649165391811913',
        status     : true, // check login status
        cookie     : true, // enable cookies to allow the server to access the session
        xfbml      : true  // parse XFBML
    });

    // Here we subscribe to the auth.authResponseChange JavaScript event. This event is fired
    // for any authentication related change, such as login, logout or session refresh. This means that
    // whenever someone who was previously logged out tries to log in again, the correct case below
    // will be handled.
    FB.Event.subscribe('auth.authResponseChange', function(response) {
        // Here we specify what we do with the response anytime this event occurs.
        if (response.status === 'connected') {
            // The response object is returned with a status field that lets the app know the current
            // login status of the person. In this case, we're handling the situation where they
            // have logged in to the app.
//            getUser();
        } else if (response.status === 'not_authorized') {
            // In this case, the person is logged into Facebook, but not into the app, so we call
            // FB.login() to prompt them to do so.
            // In real-life usage, you wouldn't want to immediately prompt someone to login
            // like this, for two reasons:
            // (1) JavaScript created popup windows are blocked by most browsers unless they
            // result from direct interaction from people using the app (such as a mouse click)
            // (2) it is a bad experience to be continually prompted to login upon page load.
            fbLogin()
        } else {
            // In this case, the person is not logged into Facebook, so we call the login()
            // function to prompt them to do so. Note that at this stage there is no indication
            // of whether they are logged into the app. If they aren't then they'll see the Login
            // dialog right after they log in to Facebook.
            // The same caveats as above apply to the FB.login() call here.
            fbLogin()
        }
    });
};

function fbLogin() {
    FB.login(function(response) {
        if (response.authResponse) {
            console.log('Welcome!  Fetching your information.... ');
            FB.api('/me', function(response) {
                console.log('Good to see you, ' + response.name + '.');
            });

        } else {
            console.log('User cancelled login or did not fully authorize.');
        }
    }, {scope: 'email'});
}

// Load the SDK asynchronously
(function(d){
    var js, id = 'facebook-jssdk', ref = d.getElementsByTagName('script')[0];
    if (d.getElementById(id)) {return;}
    js = d.createElement('script'); js.id = id; js.async = true;
    js.src = "//connect.facebook.net/en_US/all.js";
    ref.parentNode.insertBefore(js, ref);
}(document));

// Here we run a very simple test of the Graph API after login is successful.
// This testAPI() function is only called in those cases.
//function testAPI() {
//    console.log('Welcome!  Fetching your information.... ');
//    FB.api('/me', function(response) {
//        console.log('Good to see you, ' + response.name + '.');
//    });
//}


function getUser() {
    //Get info about the current logged in user
    FB.api('/me', function(response) {
//        alert("Welkom " + response.name + ", je email adres is: " +  response.email);
    });
}


//window.fbAsyncInit = function() {
//    FB.init({
//        appId      : '649165391811913',
//        channelUrl : 'http://localhost.local/',
//        status     : true, // check login status
//        cookie     : true, // enable cookies to allow the server to access the session
//        xfbml      : true  // parse XFBML
//    });
//
//    FB.Event.subscribe('auth.authResponseChange', function(response) {
//        if (response.status === 'connected') {
//            testAPI();
//        } else if (response.status === 'not_authorized') {
//            FB.login();
//        } else {
//            FB.login();
//        }
//    });
//};
//
//// Load the SDK asynchronously
//(function(d){
//    var js, id = 'facebook-jssdk', ref = d.getElementsByTagName('script')[0];
//    if (d.getElementById(id)) {return;}
//    js = d.createElement('script'); js.id = id; js.async = true;
//    js.src = "//connect.facebook.net/en_US/all.js";
//    ref.parentNode.insertBefore(js, ref);
//}(document));
//
//// Here we run a very simple test of the Graph API after login is successful.
//// This testAPI() function is only called in those cases.
//function testAPI() {
//    console.log('Welcome!  Fetching your information.... ');
//    FB.api('/me', function(response) {
//        console.log('Good to see you, ' + response.name + '.');
//    });
//}

//window.fbAsyncInit = function() {
//    FB.init({
//        appId      : '649165391811913', // Set YOUR APP ID
//        channelUrl : 'http://localhost.local/', // Channel File
//        status     : true, // check login status
//        cookie     : true, // enable cookies to allow the server to access the session
//        xfbml      : true  // parse XFBML
//    });
//
//    FB.Event.subscribe('auth.authResponseChange', function(response)
//    {
//        if (response.status === 'connected')
//        {
//            document.getElementById("message").innerHTML +=  "<br>Connected to Facebook";
//            //SUCCESS
//
//        }
//        else if (response.status === 'not_authorized')
//        {
//            document.getElementById("message").innerHTML +=  "<br>Failed to Connect";
//
//            //FAILED
//        } else
//        {
//            document.getElementById("message").innerHTML +=  "<br>Logged Out";
//
//            //UNKNOWN ERROR
//        }
//    });
//
//};
//
//function Login()
//{
//
//    FB.login(function(response) {
//        if (response.authResponse)
//        {
//            getUserInfo();
//        } else
//        {
//            console.log('User cancelled login or did not fully authorize.');
//        }
//    },{scope: 'email,user_photos,user_videos'});
//
//}
//
//function getUserInfo() {
//    FB.api('/me', function(response) {
//
//        var str="<b>Name</b> : "+response.name+"<br>";
//        str +="<b>Link: </b>"+response.link+"<br>";
//        str +="<b>Username:</b> "+response.username+"<br>";
//        str +="<b>id: </b>"+response.id+"<br>";
//        str +="<b>Email:</b> "+response.email+"<br>";
//        str +="<input type='button' value='Get Photo' onclick='getPhoto();'/>";
//        str +="<input type='button' value='Logout' onclick='Logout();'/>";
//        document.getElementById("status").innerHTML=str;
//
//    });
//}
//function getPhoto()
//{
//    FB.api('/me/picture?type=normal', function(response) {
//
//        var str="<br/><b>Pic</b> : <img src='"+response.data.url+"'/>";
//        document.getElementById("status").innerHTML+=str;
//
//    });
//
//}
//function Logout()
//{
//    FB.logout(function(){document.location.reload();});
//}
//
//// Load the SDK asynchronously
//(function(d){
//    var js, id = 'facebook-jssdk', ref = d.getElementsByTagName('script')[0];
//    if (d.getElementById(id)) {return;}
//    js = d.createElement('script'); js.id = id; js.async = true;
//    js.src = "//connect.facebook.net/en_US/all.js";
//    ref.parentNode.insertBefore(js, ref);
//}(document));