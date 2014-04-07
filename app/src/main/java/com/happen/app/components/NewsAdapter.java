package com.happen.app.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.happen.app.R;
import com.happen.app.activities.FriendsFragment;
import com.happen.app.activities.MainActivity;
import com.happen.app.util.Util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Spencer on 3/28/14.
 */
public class NewsAdapter extends BaseAdapter{
    static final String KEY_REQUESTS = "requests";
    static final String KEY_EMPTY = "empty";

    private ArrayList<MainActivity.NewsObject> data;
    private static LayoutInflater inflater = null;
    private FriendsFragment parent;

    public NewsAdapter(ArrayList<MainActivity.NewsObject> d, LayoutInflater i) {
        data = d;
        inflater = i;
    }

    public NewsAdapter(ArrayList<MainActivity.NewsObject> d, LayoutInflater i, FriendsFragment parent) {
        data = d;
        inflater = i;
        this.parent = parent;
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


        MainActivity.NewsObject news;
        if(data.size()==0)
        {
            vi = inflater.inflate(R.layout.row_news_empty, null);
            //requesterFullName.setText("empty");
        }
        else
        {
            String meTooText = " has Me-Too'ed your event.";
            String acceptText = " has accepted your friend request.";
            String requestText = " has sent you a friend request.";

            news = data.get(i);
            System.out.println("i: " + i + "news_type: " + news.type);
            if(news.type.equals(Util.NEWS_TYPE_ME_TOO)) {
                vi = inflater.inflate(R.layout.row_news_me_too, null);
                TextView txt = (TextView) vi.findViewById(R.id.news_item_txt);
                txt.setText(news.nameSource + meTooText);
            } else if(news.type.equals(Util.NEWS_TYPE_REQ_ACCEPT)){
                vi = inflater.inflate(R.layout.row_news_request_accepted, null);
                TextView txt = (TextView) vi.findViewById(R.id.news_item_txt);
                txt.setText(news.nameSource + acceptText);
            } else if(news.type.equals(Util.NEWS_TYPE_REQ_RECEIVED)){
                vi = inflater.inflate(R.layout.row_news_request_received, null);
                TextView txt = (TextView) vi.findViewById(R.id.news_item_txt);
                txt.setText(news.nameSource + requestText);
            }
        }

        return vi;
    }

    public void replace(ArrayList<MainActivity.NewsObject> d) {
        this.data = d;
        this.notifyDataSetChanged();
    }
}
