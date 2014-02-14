package com.happen.app;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Created by Spencer on 2/13/14.
 */
public class DBUtil {



    static void createUser(String username, String password, String email, String phone) {

        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);

        // other fields can be set just like with ParseObject
        user.put("phone", phone);

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Hooray! Let them use the app now.
                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                }
            }
        });



    }

//    static void createEvent(String msg, ParseUser user, String date) {
//        ParseObject event = new ParseObject("Event");
//        event.put("details", msg);
//        event.put()
//    }


}
