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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.happen.app.R;
import com.happen.app.components.EventObject;
import com.happen.app.util.MyListCache;
import com.happen.app.util.Util;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;



/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class CreateEventActivity extends Activity {

    // Values for the event text and date.
    private String mText;
    private String mDate;

    // UI references.
    private EditText mTextView;
    private Spinner mDateView;
    private Button mButton;
    private ParseObject event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_event);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setLogo(R.drawable.logo);

        mTextView = (EditText) findViewById(R.id.text);
        mTextView.setText(mText);

        mDateView = (Spinner) findViewById(R.id.date);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.date_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDateView.setAdapter(adapter);
        mButton = (Button) findViewById(R.id.create_event_button);

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
//               NavUtils.navigateUpFromSameTask(this);
                 this.finish();
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
        mTextView.setError(null);
        mButton.setClickable(false);

        // Store values at the time of the creation attempt.
        mText = mTextView.getText().toString();
        mDate = mDateView.getSelectedItem().toString();

        boolean cancel = false;
        View focusView = null;

        // Error check to see if there is sufficient info
        if (mText.isEmpty()) {
            mTextView.setError(getString(R.string.error_field_required));
            focusView = mTextView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't create new event and focus the first
            // form field with an error.
            focusView.requestFocus();
            mButton.setClickable(true);
        } else {
            Parse.initialize(this, "T67m6NTwHFuyyNavdRdFGlwNM5UiPE48l3sIP6fP", "GVaSbLvVYagIzZCd7XYLfG0H9lHJBwpUvsUKen7Z");
            createEvent(mText, ParseUser.getCurrentUser(), mDate);
        }
    }

     void createEvent(String msg, ParseUser user, String date) {
        event = new ParseObject("Event");
        event.put("details", msg);
        event.put("timeFrame", date);
        event.put("creator", user);

        if(msg.equals("")){
            onPostExecute(false);
            return;
        }
        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null){
                    MyListCache cache = MyListCache.getInstance();
                    cache.addEvent(0, new EventObject(event.get("creator").toString(),
                            (String) event.get("details"),
                            (String) event.get("timeFrame"),
                            event.getObjectId(), event));

                }
                else {
                    onPostExecute(false);
                }

            }
        });
        onPostExecute(true);


    }

    protected void onPostExecute(final Boolean success) {
        if (success) {
            Intent i = new Intent(CreateEventActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            this.startActivity(i);
        } else {
            mButton.setClickable(true);
            mTextView.setError("Error: could not create event.");
        }
    }
}
