package com.happen.app.components;

import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.happen.app.R;
import com.happen.app.activities.MyListFragment;
import com.happen.app.util.Util;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nelson on 2/14/14.
 */
public class UserListAdapter extends BaseAdapter {
    // XML node keys
    static final String KEY_EVENT_DETAILS = "eventDetails";
    static final String KEY_EMPTY = "empty";

    private ArrayList<EventObject> data;
    private static LayoutInflater inflater = null;
    private MyListFragment parent;

    public UserListAdapter(ArrayList<EventObject> d, LayoutInflater i) {
        this.data = d;
        this.inflater = i;
    }

    public UserListAdapter(ArrayList<EventObject> d, LayoutInflater i, MyListFragment p) {
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
            vi = inflater.inflate(R.layout.row_user_list, null);
        }
        TextView eventDetails = (TextView)vi.findViewById(R.id.event_details);
        ImageView button = (ImageView)vi.findViewById(R.id.me_too_checkmark);

        EventObject event;
        event = data.get(i);

        if(event.isEmpty()) {
            eventDetails.setText(R.string.no_user_list);
            return vi;
        } else {
            // Setting the values
            eventDetails.setText(event.details);
        }


        if(event!=null) {
            vi.setTag(event);
            List<ParseUser> users = event.parseObj.getList(Util.COL_ME_TOOS_ARRAY);
            if (users != null) {
                for(int j = 0; j < users.size(); j++) {
                    if(users.get(j).getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                        event.meToo = true;
                        button.setVisibility(View.VISIBLE);
                    }
                    else
                        button.setVisibility(View.INVISIBLE);
                }
            }

        }
        return vi;
    }

    public void replace(ArrayList<EventObject> d) {
        this.data = d;
        this.notifyDataSetChanged();
    }
}
