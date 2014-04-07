package com.happen.app.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.happen.app.R;
import com.happen.app.util.HappenUser;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class FindFriendActivity extends Activity implements View.OnClickListener {
    // Parse column names
    static final String COL_NUMBER = "phoneNumber";
    static final String COL_PROFILE_PIC = "profilePic";

    // Values for username at the time of the find attempt.
    private String mUsername;

    // UI references.
    private EditText mUsernameTextbox;
    private Button mButton;
    private ListView mContactsView;
    private LinearLayout mUsernameView;
    private Button mUsernameTabButton, mContactsTabButton;

    private ContactsSearchAdapter mContactsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_find_friends);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setLogo(R.drawable.logo);

        mUsernameTextbox = (EditText) findViewById(R.id.add_friends_username_textbox);
        mUsernameTextbox.setText(mUsername);
        mButton = (Button) findViewById(R.id.add_friends_username_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findFriends();
            }
        });
        mContactsView = (ListView) findViewById(R.id.add_friends_contacts_view);
        mUsernameView = (LinearLayout) findViewById(R.id.add_friends_username_view);
        mUsernameTabButton = (Button) findViewById(R.id.add_friends_username_tab_button);
        mUsernameTabButton.setOnClickListener(this);
        mContactsTabButton = (Button) findViewById(R.id.add_friends_contacts_tab_button);
        mContactsTabButton.setOnClickListener(this);
        mContactsAdapter = new ContactsSearchAdapter(null, getLayoutInflater());
        mContactsView.setAdapter(mContactsAdapter);
        mContactsAdapter.setListener(this);
    }

    private void findFriends() {
        // Reset errors
        mUsernameTextbox.setError(null);

        // Store values at the time of the login attempt.
        mUsername = mUsernameTextbox.getText().toString();
        mButton.setClickable(false);
        sendFriendRequest(mUsername);
    }

    void sendFriendRequest(String targetUsername) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("source", ParseUser.getCurrentUser().getObjectId());
        params.put("target", targetUsername);
        Log.d("FindFriendActivity", "Sending friend request from " + params.get("source") + " to " + params.get("target"));
        ParseCloud.callFunctionInBackground("sendFriendRequest", params, new FunctionCallback<String>() {
            public void done(String result, ParseException e) {
                if (e == null) {
                    onPostExecute(true);
                } else {
                    mButton.setClickable(true);
                    mUsernameTextbox.setError(e.getMessage());
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
        return true;
    }

    public void switchToUsernameSearch() {
        // Switch to Search-by-Username view
        mUsernameTabButton.setBackground(getResources().getDrawable(R.drawable.rounded_stroked_box_left_active));
        mUsernameTabButton.setTextColor(Color.parseColor("#FFFFFF"));
        mContactsTabButton.setBackground(getResources().getDrawable(R.drawable.rounded_stroked_box_right));
        mContactsTabButton.setTextColor(Color.parseColor("#3a3b49"));
        mUsernameTabButton.setClickable(false);
        mContactsTabButton.setClickable(true);
        mUsernameView.setVisibility(View.VISIBLE);
        mContactsView.setVisibility(View.GONE);
    }

    public void switchToContactsSearch() {
        // Switch to Search-by-Number view
        mUsernameTabButton.setBackground(getResources().getDrawable(R.drawable.rounded_stroked_box_left));
        mUsernameTabButton.setTextColor(Color.parseColor("#3a3b49"));
        mContactsTabButton.setBackground(getResources().getDrawable(R.drawable.rounded_stroked_box_right_active));
        mContactsTabButton.setTextColor(Color.parseColor("#FFFFFF"));
        mUsernameTabButton.setClickable(true);
        mContactsTabButton.setClickable(false);
        mUsernameView.setVisibility(View.GONE);
        mContactsView.setVisibility(View.VISIBLE);
        displayMatchingContacts();
    }

    public void displayMatchingContacts() {
        ArrayList<String> numbers = getContactList();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereContainedIn(COL_NUMBER, numbers);
        //TODO: NEED TO EXCLUDE USERS WHO ARE ALREADY FRIENDS

        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException ex) {
                if (ex == null) {
                    ArrayList<HappenUser> map = new ArrayList<HappenUser>();
                    for (ParseUser u : users) {
                        map.add(new HappenUser(u));
                    }
                    mContactsAdapter.replace(map);
                } else {
                    Log.e("FindFriendActivity", "Failed to retrieve user list that have matching phone numbers");
                }
            }
        });
    }

    private ArrayList<String> getContactList(){
        //ArrayList<Integer> contactsList = new ArrayList<Integer>();
        ArrayList<String> contactsList = new ArrayList<String>();
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if(cursor.moveToFirst())
        {
            do
            {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{ id }, null);
                    while (pCur.moveToNext())
                    {
                        String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contactNumber = contactNumber.replaceAll("[^\\d.]", "");
                        contactsList.add(contactNumber);
                        break;
                    }
                    pCur.close();
                }

            } while (cursor.moveToNext()) ;
        }

        return contactsList;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.add_friends_username_tab_button:
                switchToUsernameSearch();
                break;

            case R.id.add_friends_contacts_tab_button:
                switchToContactsSearch();
                break;

            case R.id.add_friends_contacts_entry_add_button:
                sendFriendRequest(((HappenUser)v.getTag()).getUsername());
                break;

            default:
                Log.e("FindFriendActivity", "Something weird happened...");
                break;
        }
    }


    protected void onPostExecute(final Boolean success) {
        if (success) {
            Intent i = new Intent(FindFriendActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            this.startActivity(i);
        } else {
            mUsernameTextbox.setError("Error: could not find friends.");
        }
    }


    public class ContactsSearchAdapter extends BaseAdapter {
        private ArrayList<HappenUser> data;
        private LayoutInflater inflater = null;
        private View.OnClickListener listener;
        private boolean isDataEmpty = false;

        public ContactsSearchAdapter(ArrayList<HappenUser> d, LayoutInflater i) {
            if (d == null)
                data = new ArrayList<HappenUser>();
            else
                data = d;

            inflater = i;
        }
        public int getCount() {
            return data.size();
        }
        public Object getItem(int i) {
            return i;
        }
        public long getItemId(int i) {
            return i;
        }

        public void setListener(View.OnClickListener l) {
            this.listener = l;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            View vi = view;
            if (vi == null) {
                vi = inflater.inflate(R.layout.row_add_friends_contacts, null);
            }

            TextView textFullName = (TextView)vi.findViewById(R.id.add_friends_contacts_entry_fullname);
            TextView textUsername = (TextView)vi.findViewById(R.id.add_friends_contacts_entry_username);
            TextView textPhoneNumber = (TextView)vi.findViewById(R.id.add_friends_contacts_entry_phoneNumber);
            Button addButton = (Button) vi.findViewById(R.id.add_friends_contacts_entry_add_button);
            ImageView imgProfilePic = (ImageView)vi.findViewById(R.id.profile_pic);

            HappenUser entry = data.get(i);

            if(isDataEmpty) {
                textFullName.setText("Could not find any matching user.");
                textUsername.setText("");
                textPhoneNumber.setText("");
                addButton.setVisibility(View.GONE);
                imgProfilePic.setVisibility(View.GONE);
            } else {
                // Setting the values
                textFullName.setText(entry.getFullname());
                textUsername.setText("@" + entry.getUsername());
                textPhoneNumber.setText(entry.getPhoneNumber());
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                imgProfilePic.setImageBitmap(entry.getProfilePic(width, getResources()));
                addButton.setVisibility(View.VISIBLE);
                addButton.setTag(entry);
                addButton.setOnClickListener(listener);
            }
            return vi;
        }

        public void replace(ArrayList<HappenUser> d) {
            this.data = d;

            // Remove the current user him/herself
            ParseUser pUser = ParseUser.getCurrentUser();
            int index = -1;
            for (int i = 0; i < data.size(); i++) {
                if (pUser.hasSameId(d.get(i).getParseUser()))
                    index = i;
            }
            if (index != -1)
                data.remove(index);

            // Check if the data set is empty
            if (data.size() == 0) {
                data.add(new HappenUser());
                isDataEmpty = true;
            }
            else {
                isDataEmpty = false;
            }
            this.notifyDataSetChanged();
        }

    }
}
