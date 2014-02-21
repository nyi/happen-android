package com.happen.app.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.happen.app.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Nelson on 2/20/14.
 */
public class FriendsAdapter extends BaseAdapter{
    // XML node keys
    static final String KEY_FRIENDS = "friends";

    private ArrayList<HashMap<String,String>> data;
    private static LayoutInflater inflater = null;

    public FriendsAdapter(ArrayList<HashMap<String, String>> d, LayoutInflater i) {
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

        TextView eventDetails = (TextView)vi.findViewById(R.id.friend);

        HashMap<String,String> event = new HashMap<String,String>();
        event = data.get(i);

        // Setting the values
        eventDetails.setText(event.get(KEY_FRIENDS));

        return vi;
    }

    public void replace(ArrayList<HashMap<String,String>> d) {
        this.data = d;
        this.notifyDataSetChanged();
    }
}
