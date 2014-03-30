package com.happen.app.components;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.happen.app.R;
import com.happen.app.activities.FeedFragment;
import com.happen.app.activities.MyListFragment;
import com.happen.app.util.Util;

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
    static final String KEY_OBJECT_ID = "objectId";

    static final float WIDTH_RATIO = 0.25f; // 25%

    private ArrayList<HashMap<String,String>> data;
    private ArrayList<Bitmap> pictures;
    private static LayoutInflater inflater = null;
    private FeedFragment parent;

    public EventFeedAdapter (ArrayList<HashMap<String,String>> d, ArrayList<Bitmap> p, LayoutInflater i) {
        data = d;
        pictures = p;
        inflater = i;
    }

    public EventFeedAdapter (ArrayList<HashMap<String,String>> d, ArrayList<Bitmap> p, LayoutInflater i, FeedFragment pa) {
        data = d;
        pictures = p;
        inflater = i;
        parent = pa;
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
        ImageView profilePic = (ImageView)vi.findViewById(R.id.profile_pic);
        Button meTooButton = (Button)vi.findViewById(R.id.me_too_button);

        HashMap<String,String> event = new HashMap<String,String>();
        event = data.get(i);

        // Setting the values
        fullName.setText(event.get(KEY_FULL_NAME));
        eventDetails.setText(event.get(KEY_EVENT_DETAILS));
        username.setText(event.get(KEY_USERNAME));
        timeFrame.setText(event.get(KEY_TIME_FRAME));
        profilePic.setImageBitmap(pictures.get(i));
        meTooButton.setTag(event.get(KEY_OBJECT_ID));

        if(parent!=null)
            meTooButton.setOnClickListener(parent);

        return vi;
    }

    public void replace(ArrayList<HashMap<String,String>> d, ArrayList<Bitmap> p) {
        this.data = d;
        this.pictures = p;
        this.notifyDataSetChanged();
    }
}
