package com.happen.app.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.happen.app.R;
import com.happen.app.activities.FriendsFragment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Nelson on 2/20/14.
 */
public class FriendsAdapter extends BaseAdapter{
    // XML node keys
    static final String KEY_FRIENDS = "friends";
    static final String KEY_EMPTY = "empty";
    static final String KEY_FULL_NAME = "fullName";
    static final String KEY_USERNAME = "username";

    private ArrayList<HashMap<String,FriendsFragment.FriendObject>> data;
    private static LayoutInflater inflater = null;

    public FriendsAdapter(ArrayList<HashMap<String, FriendsFragment.FriendObject>> d, LayoutInflater i) {
        data = d;
        inflater = i;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vi = view;
        if(view == null) {
            vi = inflater.inflate(R.layout.row_friends, null);
        }

        TextView friendName = (TextView)vi.findViewById(R.id.friend);

        HashMap<String,FriendsFragment.FriendObject> friend;
        friend = data.get(i);

        if(friend.containsKey(KEY_EMPTY)) {
            friendName.setText(R.string.no_friends);
        } else {
            // Setting the values
            friendName.setText(friend.get(KEY_FRIENDS).getUsername());
        }

        return vi;
    }

    public void replace(ArrayList<HashMap<String,FriendsFragment.FriendObject>> d) {
        this.data = d;
        this.notifyDataSetChanged();
    }
}
