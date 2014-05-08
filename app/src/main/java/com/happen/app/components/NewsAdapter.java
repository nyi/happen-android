package com.happen.app.components;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
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

    private ArrayList<NewsObject> data;
    private ArrayList<Bitmap> pictures;
    private static LayoutInflater inflater = null;
    private MainActivity parent;

    public NewsAdapter(ArrayList<NewsObject> d, LayoutInflater i) {
        data = d;
        inflater = i;
    }

    public NewsAdapter(ArrayList<NewsObject> d, LayoutInflater i, MainActivity parent) {
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
        NewsObject news;
        if(data.size()==0)
        {
            vi = inflater.inflate(R.layout.row_news_empty, null);
            //requesterFullName.setText("empty");
        }
        else
        {
            String meTooText = " said me too";
            String acceptText = "accepted your friend request";
            String requestText = "sent you a friend request";
            vi = inflater.inflate(R.layout.row_news, null);
            TextView txt = (TextView) vi.findViewById(R.id.news_item_txt);
            TextView name_txt = (TextView) vi.findViewById(R.id.news_name_txt);
            TextView lower_txt = (TextView) vi.findViewById(R.id.news_item_txt_lower);
            ImageView profilePic = (ImageView)vi.findViewById(R.id.profile_pic);

            news = data.get(i);
            name_txt.setText(data.get(i).nameSource);

            if(news.type.equals(Util.NEWS_TYPE_ME_TOO)) {
                txt.setText( meTooText);
                lower_txt.setText(data.get(i).event);
            } else if(news.type.equals(Util.NEWS_TYPE_REQ_ACCEPT)){
                lower_txt.setText(acceptText);
            } else if(news.type.equals(Util.NEWS_TYPE_REQ_RECEIVED)){
                lower_txt.setText(requestText);
            } else if(news.type.equals(Util.NEWS_EMPTY)){
                vi = inflater.inflate(R.layout.row_news_empty, null);
            }

            if(pictures.size() > i) {
                profilePic.setImageBitmap(pictures.get(i));
            }

            if(news.isUnread)
            {
                ImageView newsCircle = (ImageView) vi.findViewById(R.id.dot);
                newsCircle.setVisibility(View.VISIBLE);
            }
            else
            {
                ImageView newsCircle = (ImageView) vi.findViewById(R.id.dot);
                newsCircle.setVisibility(View.INVISIBLE);
            }
        }

        return vi;
    }

    public void replace(ArrayList<NewsObject> d, ArrayList<Bitmap> p) {
        this.data = d;
        this.pictures = p;
        this.notifyDataSetChanged();
    }

    public void replace(ArrayList<NewsObject> d) {
        this.data = d;
        this.notifyDataSetChanged();
    }
}
