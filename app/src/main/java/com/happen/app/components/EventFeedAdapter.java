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
 * Created by Nelson on 2/14/14.
 */
public class EventFeedAdapter extends BaseAdapter {
    // XML node keys
    static final String KEY_FULL_NAME = "fullName";
    static final String KEY_EVENT_DETAILS = "eventDetails";
    static final String KEY_USERNAME = "username";
    static final String KEY_TIME_FRAME = "timeFrame";

    private ArrayList<HashMap<String,String>> data;
    private static LayoutInflater inflater = null;

    public EventFeedAdapter (ArrayList<HashMap<String,String>> d, LayoutInflater i) {
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
            vi = inflater.inflate(R.layout.row_event_feed, null);
        }

        TextView fullName= (TextView)vi.findViewById(R.id.full_name);
        TextView eventDetails = (TextView)vi.findViewById(R.id.event_details);
        TextView username = (TextView)vi.findViewById(R.id.username);
        TextView timeFrame = (TextView)vi.findViewById(R.id.time_frame);

        HashMap<String,String> event = new HashMap<String,String>();
        event = data.get(i);

        // Setting the values
        fullName.setText(event.get(KEY_FULL_NAME));
        eventDetails.setText(event.get(KEY_EVENT_DETAILS));
        username.setText(event.get(KEY_USERNAME));
        timeFrame.setText(event.get(KEY_TIME_FRAME));

        return vi;
    }

    public void replace(ArrayList<HashMap<String,String>> d) {
        this.data = d;
        this.notifyDataSetChanged();
    }
}
