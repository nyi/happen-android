package com.happen.app.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.happen.app.R;
import com.happen.app.activities.FriendsFragment;
import com.happen.app.activities.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Spencer on 3/28/14.
 */
public class NewsAdapter extends BaseAdapter{
    static final String KEY_REQUESTS = "requests";
    static final String KEY_EMPTY = "empty";

    private ArrayList<HashMap<String, MainActivity.NewsObject>> data;
    private static LayoutInflater inflater = null;
    private FriendsFragment parent;

    public NewsAdapter(ArrayList<HashMap<String, MainActivity.NewsObject>> d, LayoutInflater i) {
        data = d;
        inflater = i;
    }

    public NewsAdapter(ArrayList<HashMap<String, MainActivity.NewsObject>> d, LayoutInflater i, FriendsFragment parent) {
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
            vi = inflater.inflate(R.layout.row_requests, null);
        }

        TextView requesterFullName = (TextView)vi.findViewById(R.id.friend_name);
        TextView requesterUsername = (TextView)vi.findViewById(R.id.friend_username);
        Button requester = (Button) vi.findViewById(R.id.accept_friend_button);

        HashMap<String, MainActivity.NewsObject> request;
        request = data.get(i);

        if(request.containsKey(KEY_EMPTY)) {
            requesterFullName.setText(R.string.no_requests);
            requesterUsername.setText("");
            requester.setVisibility(View.GONE);
        } else {
            // Setting the values
          //  requesterFullName.setText(request.get(KEY_REQUESTS));
          //  requesterUsername.setText(request.get(KEY_REQUESTS));
            requester.setVisibility(View.VISIBLE);
           // adds the request object to the button
         //   requester.setTag(request.get(KEY_REQUESTS).getRequest());

            //parent is FriendsFragment, handles the button clicks
            if(parent!=null)
                requester.setOnClickListener(parent);
        }

        return vi;
    }

    public void replace(ArrayList<HashMap<String, MainActivity.NewsObject>> d) {
        this.data = d;
        this.notifyDataSetChanged();
    }
}
