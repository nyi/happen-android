package com.happen.app.activities;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.happen.app.R;
import com.happen.app.components.EventObject;
import com.happen.app.components.MeTooImageView;
import com.happen.app.util.FlowLayout;
import com.happen.app.util.HappenUser;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventDetailsFragment extends Fragment  implements View.OnClickListener{

    // XML node keys
    static final String KEY_PROFILE_PIC = "profilePic";


    // Percentage of profile picture width relative to screen size
    static final float WIDTH_RATIO = 0.25f; // 25%


    TextView eventText;
    EventObject event;
    HashMap<String, ParseObject> myListEventCache;
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

    public static EventDetailsFragment newInstance(EventObject eventObj) {
        EventDetailsFragment fragment = new EventDetailsFragment(eventObj);
        Bundle args = new Bundle();

        //for(int i = 0;)
            /*args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);*/
        return fragment;
    }

    public static EventDetailsFragment newInstance(EventObject eventObj, HashMap<String, ParseObject> cache) {
        EventDetailsFragment fragment = new EventDetailsFragment(eventObj, cache);
        Bundle args = new Bundle();

        //for(int i = 0;)
            /*args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);*/
        return fragment;
    }

    public EventDetailsFragment() {

    }

    public EventDetailsFragment(EventObject eventObj) {
        this.event = eventObj;
    }
    public EventDetailsFragment(EventObject eventObj, HashMap<String, ParseObject> cache) {
        this.event = eventObj;
        this.myListEventCache = cache;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the MyList fragment layout
        View v = inflater.inflate(R.layout.fragment_event, container, false);


        eventText = (TextView)v.findViewById(R.id.event_details);
        flowLayout = (FlowLayout) v.findViewById(R.id.me_too_list);
        deleteButton = (Button) v.findViewById(R.id.delete_button);
        activity = getActivity();

        // Set full name and user handle
        eventText.setText(this.event.details);

        ArrayList<HappenUser> meToos = new ArrayList<HappenUser>();
        ParseObject event;
        if((event = myListEventCache.get(this.event.objectId)) != null)
        {
            ParseRelation relation = event.getRelation(Util.COL_ME_TOOS);
            ParseQuery query = relation.getQuery();
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> object, ParseException e) {
                    if (e == null) {
                        for(int i = 0; i  < object.size(); i++)
                        {
                            ParseFile parsePic = (ParseFile)object.get(i).get(KEY_PROFILE_PIC);
                            MeTooImageView imageView = new MeTooImageView(getActivity());
                            if (parsePic == null) {
                                Log.e("UserListFragment", "Failed to create ParseFile object");

                                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.defaultprofile);

                                // Get screen dimensions and calculate desired profile picture size
                                Display display = getActivity().getWindowManager().getDefaultDisplay();
                                Point size = new Point();
                                display.getSize(size);
                                int width = size.x;

                                bitmap = Util.circularCrop(bitmap, (int) (width * WIDTH_RATIO / 2));
                                imageView.setImageBitmap(bitmap);
                            }
                            else {
                                parsePic.getDataInBackground(new GetDataCallback() {
                                    @Override
                                    public void done(byte[] bytes, ParseException e) {
                                        if (e == null) {
                                            if (bytes == null || bytes.length == 0)
                                                Log.e("UserListFragment", "Received invalid byte array for profile picture.");
                                            else {
                                                Bitmap bitmap;
                                                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                                                // Get screen dimensions and calculate desired profile picture size
                                                Display display = getActivity().getWindowManager().getDefaultDisplay();
                                                Point size = new Point();
                                                display.getSize(size);
                                                int width = size.x;
                                                MeTooImageView dynamicImg = new MeTooImageView(activity);
                                                bitmap = Util.circularCrop(bitmap, (int) (width * WIDTH_RATIO / 2));
                                                dynamicImg.setImageBitmap(bitmap);
                                                flowLayout.addView(dynamicImg);

                                            }
                                        }
                                        else {
                                            Log.e("UserListFragment", "Failed to retrieve profile picture for user ");
                                            Log.e("UserListFragment", "Error: " + e.getMessage());
                                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.defaultprofile);

                                            // Get screen dimensions and calculate desired profile picture size
                                            Display display = getActivity().getWindowManager().getDefaultDisplay();
                                            Point size = new Point();
                                            display.getSize(size);
                                            int width = size.x;
                                            MeTooImageView dynamicImg = new MeTooImageView(activity);
                                            bitmap = Util.circularCrop(bitmap, (int) (width * WIDTH_RATIO / 2));
                                            dynamicImg.setImageBitmap(bitmap);
                                            flowLayout.addView(dynamicImg);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }

            });

        }

        deleteButton.setOnClickListener(this);


        return v;
    }

    public void deleteEvent(){
        HashMap<String, Object> params = new HashMap<String, Object>();
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
                deleteEvent();
                break;
        }
    }

}
