package com.happen.app.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.happen.app.R;
import com.happen.app.activities.MyListFragment;
import com.happen.app.util.Util;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Spencer on 4/17/14.
 */
public class MyListAdapter extends BaseAdapter {
    // XML node keys
    static final String KEY_EVENT_DETAILS = "eventDetails";
    static final String KEY_EMPTY = "empty";

    private ArrayList<EventObject> data;
    private static LayoutInflater inflater = null;
    private MyListFragment parent;

    public MyListAdapter(ArrayList<EventObject> d, LayoutInflater i) {
        this.data = d;
        this.inflater = i;
    }

    public MyListAdapter(ArrayList<EventObject> d, LayoutInflater i, MyListFragment p) {
        this.data = d;
        this.inflater = i;
        this.parent = p;
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
            vi = inflater.inflate(R.layout.row_my_list, null);
        }
        TextView eventDetails = (TextView)vi.findViewById(R.id.event_details);

        EventObject event;
        event = data.get(i);

        if(event.isEmpty()) {
            eventDetails.setText("No events here. Create more events!");
            return vi;
        } else {
            // Setting the values
            eventDetails.setText(event.details);
        }

        //if parent!=null it means list adapter is used by mylist (and should have meTOOCount displayed
        //this should really be split into two classes
        if(parent!=null)
        {
            vi.setOnClickListener(parent);
            TextView meTooCount = (TextView) vi.findViewById(R.id.me_too_count);
            Integer count = (Integer) event.parseObj.get("meTooCount");
            if(count != null && count > 0)
                meTooCount.setText("+" + count.toString());
            else
                meTooCount.setText("");
        }
        if(event!=null) {
            vi.setTag(event);
        }
        return vi;
    }

    public void replace(ArrayList<EventObject> d) {
        this.data = d;
        this.notifyDataSetChanged();
    }
}
