package com.happen.app.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.happen.app.R;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class FindFriendActivity extends Activity {
    // Parse column names
    static final String TABLE_USER = "User";
    static final String COL_USERNAME = "username";

    // Values for username at the time of the find attempt.
    private String mUsername;

    // UI references.
    private EditText mUsernameView;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_find_friends);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setLogo(R.drawable.logo);

        mUsernameView = (EditText) findViewById(R.id.text);
        mUsernameView.setText(mUsername);
        mButton = (Button) findViewById(R.id.search_for_friends_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findFriends();
            }
        });
    }

    private void findFriends() {
        // Reset errors
        mUsernameView.setError(null);

        // Store values at the time of the login attempt.
        mUsername = mUsernameView.getText().toString();
        mButton.setClickable(false);
        sendFriendRequest(ParseUser.getCurrentUser(), null, mUsername);
    }

    void sendFriendRequest(ParseUser source, ParseUser target, String targetUsername) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("source", ParseUser.getCurrentUser().getObjectId());
        params.put("target", targetUsername);
        ParseCloud.callFunctionInBackground("sendFriendRequest", params, new FunctionCallback<String>() {
            public void done(String result, ParseException e) {
                if (e == null) {
                    onPostExecute(true);
                } else {
                    mButton.setClickable(true);
                    mUsernameView.setError(e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
       // getMenuInflater().inflate(R.menu.signup, menu);
        return true;
    }



    protected void onPostExecute(final Boolean success) {
        if (success) {
            Intent i = new Intent(FindFriendActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            this.startActivity(i);
        } else {
            mUsernameView.setError("Error: could not find friends.");
        }
    }
}
