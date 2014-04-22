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

    static final float WIDTH_RATIO = 0.25f; // 25%

    private ArrayList<EventObject> data;
    private ArrayList<Bitmap> pictures;
    private static LayoutInflater inflater = null;
    private FeedFragment parent;

    public EventFeedAdapter (ArrayList<EventObject> d, ArrayList<Bitmap> p, LayoutInflater i) {
        data = d;
        pictures = p;
        inflater = i;
    }

    public EventFeedAdapter (ArrayList<EventObject> d, ArrayList<Bitmap> p, LayoutInflater i, FeedFragment pa) {
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
        TextView timeFrame = (TextView)vi.findViewById(R.id.time_frame);
        ImageView profilePic = (ImageView)vi.findViewById(R.id.profile_pic);
        Button meTooButton = (Button)vi.findViewById(R.id.me_too_button);

        EventObject event;
        event = data.get(i);

        if(event.isEmpty()) {
            vi = inflater.inflate(R.layout.row_event_empty, null);

        } else {
            // Setting the values
            fullName.setText(event.owner);
            eventDetails.setText(event.details);
            timeFrame.setText(event.timeFrame);
            if(pictures.size() > i) {
                profilePic.setImageBitmap(pictures.get(i));
            }
            meTooButton.setTag(event.objectId);

            if(parent!=null)
                meTooButton.setOnClickListener(parent);
        }

        return vi;
    }

    public void replace(ArrayList<EventObject> d, ArrayList<Bitmap> p) {
        this.data = d;
        this.pictures = p;
        this.notifyDataSetChanged();
    }

    public void removeRow(int position) {
        this.data.remove(position);
        this.pictures.remove(position);
        this.notifyDataSetChanged();
    }
}
