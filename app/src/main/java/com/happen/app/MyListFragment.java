package com.happen.app;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 2/10/14.
 */
public class MyListFragment extends ListFragment{
    String[] numbers_text = new String[] { "mylist_one", "mylist_two", "mylist_three", "mylist_four",
            "mylist_five", "mylist_six", "mylist_seven", "mylist_eight", "mylist_nine", "mylist_ten", "mylist_eleven",
            "mylist_twelve", "mylist_thirteen", "mylist_fourteen", "mylist_fifteen" };
    String[] numbers_digits = new String[] { "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "10", "11", "12", "13", "14", "15" };
    private static final String ARG_SECTION_NUMBER = "section_number";
    ArrayAdapter<String> adapter;

    public static MyListFragment newInstance(int sectionNumber) {
        MyListFragment fragment = new MyListFragment();
        Bundle args = new Bundle();
        //for(int i = 0;)
            /*args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);*/
        return fragment;
    }

    public MyListFragment() {

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //do something
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        List<String> blankList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(
                inflater.getContext(), android.R.layout.simple_list_item_1,
                blankList);
        setListAdapter(adapter);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.d("score", "Retrieved " + object.size() + " scores");
                    List<String> eventList = new ArrayList<String>();
                    for(int i = 0; i < object.size(); i++) {
                        eventList.add(object.get(i).getString("details"));
                    }
                    adapter.clear();
                    adapter.addAll(eventList);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
