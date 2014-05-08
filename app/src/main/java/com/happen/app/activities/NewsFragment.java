package com.happen.app.activities;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.happen.app.R;
import com.happen.app.components.NewsAdapter;
import com.happen.app.components.NewsObject;
import com.happen.app.util.Util;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Spencer on 5/07/14.
 */
public class NewsFragment extends Fragment implements View.OnClickListener{

    // Percentage of profile picture width relative to screen size
    static final float WIDTH_RATIO = 0.25f; // 25%

    NewsAdapter newsAdapter;
    ArrayList<NewsObject> newsList;
    ListView listview;
    MainActivity main;
    MainActivity.SectionsPagerAdapter pager;
    ViewPager viewPager;
    public  ArrayList<Bitmap> profPictures;
    NewsFragment self;

    public static NewsFragment newInstance() {
        NewsFragment fragment = new NewsFragment();
        return fragment;
    }

    public NewsFragment() {
        self = this;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_news, container, false);
        listview = (ListView)v.findViewById(R.id.news_list);
        newsList = new ArrayList<NewsObject>();
        newsAdapter = new NewsAdapter(newsList, inflater);
        listview.setAdapter(newsAdapter);
        queryNews();
        return v;
    }

    public void queryNews()
    {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Util.TABLE_NEWS);
        profPictures = new ArrayList<Bitmap>();

        query.orderByDescending(Util.COL_CREATED_AT);
        query.include(Util.COL_TARGET);
        query.include(Util.COL_SOURCE);
        query.include(Util.COL_EVENT);
        query.whereEqualTo(Util.COL_TARGET, ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.d("score", "Retrieved " + object.size() + " scores");
                    newsList = new ArrayList<NewsObject>();
                    if(object.size()==0)
                    {
                        NewsObject newsObj = new NewsObject(Util.NEWS_EMPTY, null, null, false);
                        newsList.add(newsObj);
                    }
                    for (int i = 0; i < object.size(); i++) {
                        ParseUser requester = object.get(i).getParseUser(Util.COL_SOURCE);
                        ParseUser target = object.get(i).getParseUser(Util.COL_TARGET);
                        String eventType = (String)object.get(i).get(Util.COL_TYPE);
                        boolean isUnread = (Boolean)object.get(i).get(Util.COL_UNREAD);
                        NewsObject newsObj;
                        String sourceName = requester.getString(Util.COL_FIRST_NAME) + " " + requester.getString(Util.COL_LAST_NAME);
                        String targetName = target.getString(Util.COL_FIRST_NAME) + " " + target.getString(Util.COL_LAST_NAME);
                        if(eventType.equals("ME_TOO"))
                        {
                            String event = (String)((ParseObject)object.get(i).get(Util.COL_EVENT)).get(Util.COL_DETAILS);
                            newsObj = new NewsObject(eventType, targetName, sourceName, event, isUnread);
                        }
                        else
                        {
                            newsObj = new NewsObject(eventType, targetName, sourceName, isUnread);
                        }
                        newsList.add(newsObj);
                        byte[] file = new byte[0];
                        try {
                            boolean imgNotFound = true;
                            ParseFile pfile = requester.getParseFile(Util.COL_PROFILE_PIC);
                            if(pfile!=null) {
                                file = pfile.getData();
                                Bitmap image = BitmapFactory.decodeByteArray(file, 0, file.length);
                                // Get screen dimensions and calculate desired profile picture size
                                Display display = self.getActivity().getWindowManager().getDefaultDisplay();

                                Point size = new Point();
                                display.getSize(size);
                                int width = size.x;
                                if(image!=null) {
                                    imgNotFound=false;
                                    image = Util.circularCrop(image, (int) (width * Util.WIDTH_RATIO / 2));
                                    profPictures.add(image);
                                }
                            }
                            if (imgNotFound){
                                Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.defaultprofile);

                                // Get screen dimensions and calculate desired profile picture size
                                Display display = self.getActivity().getWindowManager().getDefaultDisplay();
                                Point size = new Point();
                                display.getSize(size);
                                int width = size.x;

                                image = Util.circularCrop(image, (int) (width * Util.WIDTH_RATIO / 2));
                                profPictures.add(image);
                            }
                        }
                        catch(Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                    newsAdapter.replace(newsList, profPictures);
                    newsAdapter.notifyDataSetChanged();

                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {


        }
    }
}
