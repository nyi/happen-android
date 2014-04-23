package com.happen.app.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.happen.app.R;
import com.happen.app.activities.FriendsFragment;

import java.util.ArrayList;

/**
 * Created by Nelson on 2/20/14.
 */
public class RequestsAdapter extends BaseAdapter{

    private ArrayList<FriendsFragment.FriendObject> data;
    private static LayoutInflater inflater = null;
    private FriendsFragment parent;

    public RequestsAdapter(ArrayList<FriendsFragment.FriendObject> d, LayoutInflater i) {
        data = d;
        inflater = i;
    }

    public RequestsAdapter(ArrayList<FriendsFragment.FriendObject> d, LayoutInflater i, FriendsFragment parent) {
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
            vi = inflater.inflate(R.layout.row_request, null);
        }
        TextView requesterFullName = (TextView)vi.findViewById(R.id.friend_name);
        TextView requesterUsername = (TextView)vi.findViewById(R.id.friend_username);
        ImageView requesterProfilePic = (ImageView)vi.findViewById(R.id.profile_pic);

        Button acceptButton = (Button) vi.findViewById(R.id.accept_friend_button);
        Button declineButton = (Button) vi.findViewById(R.id.decline_friend_button);

        FriendsFragment.FriendObject request;
        request = data.get(i);

        if(request.isEmpty()) {
            vi = inflater.inflate(R.layout.row_request_empty, null);
        } else {
            // Setting the values
            requesterFullName.setText(request.getFullName());
            requesterUsername.setText("@" + request.getUsername());
            requesterProfilePic.setImageBitmap(request.getProfPic());
            acceptButton.setVisibility(View.VISIBLE);
            declineButton.setVisibility(View.VISIBLE);
            //adds the request object to the button
            acceptButton.setTag(request.getRequest());
            declineButton.setTag(request.getRequest());

            //parent is FriendsFragment, handles the button clicks
            if(parent!=null)
                acceptButton.setOnClickListener(parent);
                declineButton.setOnClickListener(parent);
        }

        return vi;
    }

    public void replace(ArrayList<FriendsFragment.FriendObject> d) {
        this.data = d;
        this.notifyDataSetChanged();
    }

    public void removeRow(int position) {
        this.data.remove(position);
        if(data.isEmpty()) {
            // Do something to show that it is empty
        }
        this.notifyDataSetChanged();
    }
}
