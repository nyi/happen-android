package com.happen.app.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.happen.app.R;
import com.happen.app.components.EventObject;
import com.happen.app.components.MeTooImageView;
import com.happen.app.util.FlowLayout;
import com.happen.app.util.HappenUser;
import com.happen.app.util.HappenUserCache;
import com.happen.app.util.MyListCache;
import com.happen.app.util.Util;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetDataCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventDetailsFragment extends Fragment  implements View.OnClickListener{

    // Percentage of profile picture width relative to screen size
    static final float WIDTH_RATIO = 0.25f; // 25%


    TextView eventText;
    EventObject event;
    MyListCache myListCache;
    Activity activity;
    FlowLayout flowLayout;
    Button deleteButton;


    public static EventDetailsFragment newInstance(int sectionNumber) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        //for(int i = 0;)
            /*args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);*/
        return fragment;
    }

    public static EventDetailsFragment newInstance(String objectId) {
        EventDetailsFragment fragment;
        if(objectId!=null)
           fragment = new EventDetailsFragment(objectId);
        else
            fragment = new EventDetailsFragment();
        Bundle args = new Bundle();

        //for(int i = 0;)
            /*args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);*/
        return fragment;
    }


    public EventDetailsFragment() {

    }

    public EventDetailsFragment(String objectId) {
        MyListCache cache = MyListCache.getInstance();
        this.event = cache.get(objectId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("evetn details", "menu item selected!");
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager manager = getFragmentManager();
                FragmentTransaction ft = manager.beginTransaction();
                ft.remove(this);
                ft.commit();
                manager.popBackStack();
                getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
                getActivity().getActionBar().setHomeButtonEnabled(false);

                return true;

            // Other case statements...

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the MyList fragment layout
        View v = inflater.inflate(R.layout.fragment_event, container, false);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);


        eventText = (TextView)v.findViewById(R.id.event_details);
        flowLayout = (FlowLayout) v.findViewById(R.id.me_too_list);
        deleteButton = (Button) v.findViewById(R.id.delete_button);
        activity = getActivity();

        // Set full name and user handle
        if(this.event != null && this.event.details != null) {
            eventText.setText(this.event.details);
        }

        ArrayList<HappenUser> meToos = new ArrayList<HappenUser>();
        ParseObject event;
        myListCache = MyListCache.getInstance();
        EventObject eventObj = myListCache.get(this.event.objectId);
        if(eventObj != null)
        {
            List<ParseUser> meToosList = eventObj.parseObj.getList(Util.COL_ME_TOOS_ARRAY);
            if(meToosList!=null)
            {
                for(int i = 0; i < meToosList.size(); i++)
                {
                    MeTooImageView imageView = new MeTooImageView(getActivity());
                    HappenUserCache userCache = HappenUserCache.getInstance();
                    HappenUser user = userCache.getUser(meToosList.get(i).getObjectId());
                    if(user == null)
                    {
                        Log.d("eventdetails", "user not found in cache - something went wrong");
                        user = new HappenUser(meToosList.get(i));
                    }
                    Display display = getActivity().getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int width = size.x;
                    imageView.setImageBitmap(user.getProfilePic(width, getResources()));
                    flowLayout.addView(imageView);
                }
            }

        }

        deleteButton.setOnClickListener(this);
        return v;
    }

    public void deleteEvent(){
        HashMap<String, Object> params = new HashMap<String, Object>();
        if(myListCache==null)
            myListCache = MyListCache.getInstance();
        myListCache.removeEvent(event.objectId);
        params.put("eventId", event.objectId);
        ParseCloud.callFunctionInBackground("deleteEvent", params, new FunctionCallback<String>() {
            public void done(String ret, ParseException e) {
                if (e == null) {
                    System.out.println("success!");
                    //Success
                } else {
                    System.out.print(e.getMessage());
                    //Error adding friend
                }

            }
        });
        ((MainActivity)getActivity()).replaceMyListPage(null);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case(R.id.delete_button):
                deleteButton.setClickable(false);
                DeleteDialog dialog = new DeleteDialog();
                dialog.show(this.getFragmentManager(), "delete");
                deleteButton.setClickable(true);
                break;
        }
    }

    public class DeleteDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Are you sure?")
                    .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            deleteEvent();
                        }
                    })
                    .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

}
