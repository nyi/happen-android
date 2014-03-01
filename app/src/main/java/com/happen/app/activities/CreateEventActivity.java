package com.happen.app.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.happen.app.R;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class CreateEventActivity extends Activity {

    // Values for email and password at the time of the signup attempt.
    private String mText;
    private String mDate;

    // UI references.
    private EditText mTextView;
    private EditText mDateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_event);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setLogo(R.drawable.logo);

        mTextView = (EditText) findViewById(R.id.text);
        mTextView.setText(mText);

        mDateView = (EditText) findViewById(R.id.date);
        mDateView.setText(mDate);



        findViewById(R.id.create_event_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEvent();
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


    public void createEvent() {

        // Reset errors.
        mDateView.setError(null);
        mTextView.setError(null);


        // Store values at the time of the login attempt.
        mText = mTextView.getText().toString();

        mDate = mDateView.getText().toString();

        boolean cancel = false;
        View focusView = null;




        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            Parse.initialize(this, "T67m6NTwHFuyyNavdRdFGlwNM5UiPE48l3sIP6fP", "GVaSbLvVYagIzZCd7XYLfG0H9lHJBwpUvsUKen7Z");
            createEvent(mText, ParseUser.getCurrentUser(), mDate);
        }
    }

     void createEvent(String msg, ParseUser user, String date) {
        ParseObject event = new ParseObject("Event");
        event.put("details", msg);
        event.put("timeFrame", date);
        event.put("creator", user);
        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null){
                    onPostExecute(true);
                }
                else {
                    onPostExecute(false);
                }

            }
        });
    }

    protected void onPostExecute(final Boolean success) {
        if (success) {
            Intent i = new Intent(CreateEventActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            this.startActivity(i);
        } else {
            mTextView.setError("Error: could not create event.");
        }
    }
}
