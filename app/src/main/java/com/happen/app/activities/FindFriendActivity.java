package com.happen.app.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.happen.app.R;
import com.happen.app.util.HappenUser;
import com.happen.app.util.HappenUserCache;
import com.happen.app.util.Util;
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

    // Percentage of profile picture width relative to screen size
    static final float WIDTH_RATIO = 0.25f; // 25%

    // Values for username at the time of the find attempt.
    private String mUsername;

    // UI references.
    private EditText mUsernameTextbox;
    private Button mButton;
    private ListView mContactsView;
    private FrameLayout mUsernameView;
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
        mUsernameView = (FrameLayout) findViewById(R.id.add_friends_username_view);
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
        mButton.setClickable(true);
    }

    private void sendFriendRequest(String targetUsername) {
        // check if the input is empty
        if (targetUsername.isEmpty()) {
            mUsernameTextbox.setError("You need to type a username.");
            mButton.setClickable(true);
            return;
        }

        // check if the user is adding themself
        String currentUsername = ParseUser.getCurrentUser().getUsername();
        if (currentUsername.equals(targetUsername)) {
            mUsernameTextbox.setError("You cannot add yourself!");
            mButton.setClickable(true);
            return;
        }
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("source", ParseUser.getCurrentUser().getObjectId());
        params.put("target", targetUsername);
        Log.d("FindFriendActivity", "Sending friend request from " + params.get("source") + " to " + params.get("target"));
        ParseCloud.callFunctionInBackground("sendFriendRequest", params, new FunctionCallback<String>() {
            public void done(String result, ParseException e) {
                if (e == null) {
                    onPostExecute(true);
                } else {
                    mUsernameTextbox.setError(e.getMessage());
                    onPostExecute(false);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
                this.finish();
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
        final HappenUserCache userCache = HappenUserCache.getInstance();
        query.whereContainedIn(COL_NUMBER, numbers);

        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException ex) {
                if (ex == null) {
                    ArrayList<HappenUser> map = new ArrayList<HappenUser>();
                    for (ParseUser u : users) {
                        /*
                        boolean flag = true;
                        for (ParseObject friend : friendList) {
                            if (((ParseUser)friend).getObjectId().equals(u.getObjectId())) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            map.add(new HappenUser(u));
                        }
                        */
                        if (!userCache.isUserCached(u)) {
                            userCache.addUser(new HappenUser(u));
                        }
                        map.add(userCache.getUser(u.getObjectId()));
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

            case R.id.add_contact_button:
                sendFriendRequest(((HappenUser)v.getTag()).getUsername());
                break;

            default:
                Log.e("FindFriendActivity", "Something weird happened...");
                break;
        }
    }


    protected void onPostExecute(final Boolean success) {
        if (success) {
            mUsernameTextbox.setText("");
            Context context = getApplicationContext();
            CharSequence text = getString(R.string.request_sent);
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
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
                vi = inflater.inflate(R.layout.row_add_from_contacts, null);
            }

            TextView textFullName = (TextView)vi.findViewById(R.id.contact_name);
            TextView textUsername = (TextView)vi.findViewById(R.id.contact_username);
            ImageView addButton = (ImageView) vi.findViewById(R.id.add_contact_button);
            ImageView imgProfilePic = (ImageView)vi.findViewById(R.id.profile_pic);

            HappenUser entry = data.get(i);

            if(isDataEmpty) {
                textFullName.setText("Could not find any matching user.");
                textUsername.setText("");
                addButton.setVisibility(View.GONE);
                imgProfilePic.setVisibility(View.GONE);
            } else {
                // Setting the values
                textFullName.setText(entry.getFullname());
                textUsername.setText("@" + entry.getUsername());
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;

                Bitmap image = entry.getProfilePic(width, getResources());
                if(image != null){
                    imgProfilePic.setImageBitmap(image);
                } else {
                    Bitmap squareImage = BitmapFactory.decodeResource(getResources(), R.drawable.defaultprofile);
                    image = Util.circularCrop(squareImage, (int) (width * WIDTH_RATIO / 2));
                    imgProfilePic.setImageBitmap(image);
                }

                HappenUserCache userCache = HappenUserCache.getInstance();

                if (userCache.getCurrentUser().isFriendsWith(entry)) {
                    addButton.setVisibility(View.VISIBLE);
                    addButton.setImageDrawable(getResources().getDrawable(R.drawable.friendadded));
                    addButton.setTag(entry);
                    addButton.setOnClickListener(listener);
                    addButton.setClickable(false);
                }
                else {
                    addButton.setVisibility(View.VISIBLE);
                    addButton.setTag(entry);
                    addButton.setOnClickListener(listener);
                    addButton.setClickable(true);
                }
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
