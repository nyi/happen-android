package com.happen.app;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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


    public static MyListFragment newInstance(int sectionNumber) {
        MyListFragment fragment = new MyListFragment();
        Bundle args = new Bundle();
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
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                inflater.getContext(), android.R.layout.simple_list_item_1,
                numbers_text);
        setListAdapter(adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
