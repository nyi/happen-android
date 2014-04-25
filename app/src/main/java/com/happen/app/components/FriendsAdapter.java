package com.happen.app.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.happen.app.R;
import com.happen.app.activities.FriendsFragment;

import java.util.ArrayList;

/**
 * Created by Nelson on 2/20/14.
 */
public class FriendsAdapter extends BaseAdapter{

    private ArrayList<FriendsFragment.FriendObject> data;
    private static LayoutInflater inflater = null;
    private FriendsFragment parent;

    public FriendsAdapter(ArrayList<FriendsFragment.FriendObject> d, LayoutInflater i) {
        data = d;
        inflater = i;
    }

    public FriendsAdapter(ArrayList<FriendsFragment.FriendObject> d, LayoutInflater i, FriendsFragment parent) {
        data = d;
        inflater = i;
        this.parent = parent;
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
            vi = inflater.inflate(R.layout.row_friend, null);
        }

        TextView friendName = (TextView)vi.findViewById(R.id.friend);
        TextView friendUsername = (TextView)vi.findViewById(R.id.friend_username);
        ImageView profPic = (ImageView)vi.findViewById(R.id.profile_pic);

        FriendsFragment.FriendObject friend;
        friend = data.get(i);

        if(friend.isEmpty()) {
            vi = inflater.inflate(R.layout.row_friend_empty, null);
        } else {
            // Setting the values
            friendName.setText(friend.getFullName());
            friendUsername.setText("@"+friend.getUsername());
            profPic.setImageBitmap(friend.getProfPic());
            vi.setTag(friend.getUsername());
            if(parent!=null)
                vi.setOnClickListener(parent);
        }

        return vi;
    }

    public void replace(ArrayList<FriendsFragment.FriendObject> d) {
        this.data = d;
        this.notifyDataSetChanged();
    }
}
