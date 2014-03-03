package com.happen.app.activities;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.happen.app.R;
import com.happen.app.util.NonSwipeableViewPager;
import com.parse.Parse;
import com.parse.ParseUser;

public class MainActivity extends Activity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    NonSwipeableViewPager mViewPager;

    protected SimpleFeedFragment simpleFeedFragment;
    protected FeedFragment feedFragment;
    protected MyListFragment mylistFragment;
    protected Menu myMenu;
    public enum Pages {FEED, FRIENDS, MY_LIST, USER_LIST};
    public Pages currentPage;
    boolean removeFriendListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        Parse.initialize(this, "T67m6NTwHFuyyNavdRdFGlwNM5UiPE48l3sIP6fP", "GVaSbLvVYagIzZCd7XYLfG0H9lHJBwpUvsUKen7Z");
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setLogo(R.drawable.logo);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        //actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        mViewPager = (NonSwipeableViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(10);
        // Create the friendsAdapter that will return a fragment for each of the four
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager(), this);

        // Set up the ViewPager with the sections friendsAdapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);
        removeFriendListFragment = false;
        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(state == ViewPager.SCROLL_STATE_IDLE && currentPage!=Pages.USER_LIST && removeFriendListFragment){
                    mSectionsPagerAdapter.removeFriendPage();
                    removeFriendListFragment=false;
                }

            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the friendsAdapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        currentPage = Pages.FEED;
    }

    private void setOptionTitle(int id, String title)
    {
        MenuItem item = myMenu.findItem(id);
        item.setTitle(title);
    }

    private void setOptionIcon(int id, int iconRes)
    {
        MenuItem item = myMenu.findItem(id);
        item.setIcon(iconRes);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        myMenu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_create) {
            if(currentPage == Pages.FEED || currentPage == Pages.MY_LIST)
                switchToCreateEventView();
            else if(currentPage == Pages.FRIENDS)
                switchToCreateFriendView();
            return true;
        }
        else if (id == R.id.action_news) {

        }
        return super.onOptionsItemSelected(item);
    }

    public void switchToCreateEventView() {
        Intent i = new Intent(MainActivity.this, CreateEventActivity.class);
     //   i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        this.startActivity(i);
    }

    public void switchToCreateFriendView() {
        Intent i = new Intent(MainActivity.this, FindFriendActivity.class);
        //   i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        this.startActivity(i);
    }

    public void switchToFriendList(ParseUser user) {
        mSectionsPagerAdapter.addFriendPage(user);
        currentPage = Pages.USER_LIST;
        this.removeFriendListFragment=true;
        mViewPager.setCurrentItem(mSectionsPagerAdapter.getFriendPage());
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        if(myMenu!=null){
            switch(tab.getPosition()) {
                case 0:
                    currentPage = Pages.FEED;
                    switchMenuToAddEvent();
                    break;
                case 1:
                    currentPage = Pages.FRIENDS;
                    switchMenuToAddFriend();
                    break;
                case 2:
                    currentPage = Pages.MY_LIST;
                    switchMenuToAddEvent();
                    break;
            }
        }
        int newPage;
        if(currentPage==Pages.MY_LIST)
        {
            newPage = mSectionsPagerAdapter.getMyListIndex();
        }
        else
        {
            newPage = tab.getPosition();
        }
        //mSectionsPagerAdapter.removeFriendPage();
        mViewPager.setCurrentItem(newPage);
        //mSectionsPagerAdapter.removeFriendPage();

    }

    public void switchMenuToAddFriend() {
        setOptionTitle(R.id.action_create,"Add Friend");
        setOptionIcon(R.id.action_create, R.drawable.tab_icons_add_friend);
    }

    public void switchMenuToAddEvent() {
        setOptionTitle(R.id.action_create,"Add Event");
        setOptionIcon(R.id.action_create, R.drawable.tab_icons_add_event);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        if(currentPage==Pages.USER_LIST) {
            currentPage=Pages.FRIENDS;
            mViewPager.setCurrentItem(tab.getPosition());
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        //
        ArrayList<Pages> pagesList;
        ParseUser user;
        Fragment feed, friends, mylist;
        //used to ss t
        MainActivity parent;
        boolean userlist;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            init();
        }

        public SectionsPagerAdapter(FragmentManager fm, MainActivity main) {
            super(fm);
            init();
            this.parent = main;
        }

        public void init() {
            pagesList = new ArrayList<Pages>();
            feed = new FeedFragment();
            friends = new FriendsFragment(this.parent);
            mylist = new UserListFragment(ParseUser.getCurrentUser());
            userlist=false;
            pagesList.add(Pages.FEED);
            pagesList.add(Pages.FRIENDS);
            pagesList.add(Pages.MY_LIST);

        }

        public void insertPage(Pages p, int index) {
            pagesList.add(index, p);
        }

        public void removePage(int index) {
            if(index>=0)
                pagesList.remove(index);
        }

        public void addFriendPage(ParseUser user) {
            this.setUser(user);
            insertPage(Pages.USER_LIST, 2);
            notifyDataSetChanged();
        }

        public int getFriendPage() {
            return pagesList.indexOf(Pages.USER_LIST);
        }

        public void removeFriendPage() {
            if(pagesList.indexOf(Pages.USER_LIST)>-1) {
                removePage(pagesList.indexOf(Pages.USER_LIST));
                notifyDataSetChanged();
            }
            else
                Log.e("MainActivity", "Could not remove page");
        }

        public void setUser(ParseUser user) {
            this.user = user;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return translatePageEnum(pagesList.get(position));
        }

        @Override
        public int getItemPosition(Object object) {
            for(int i = 0; i < pagesList.size(); i++) {
                if(pagesList.get(i)==object)
                    return POSITION_UNCHANGED;
            }
            return POSITION_NONE;
        }

        public Fragment translatePageEnum(Pages p) {
            switch(p) {
                case FEED:
                    return FeedFragment.newInstance(0);
                case FRIENDS:
                    return FriendsFragment.newInstance(this.parent);
                case MY_LIST:
                    return UserListFragment.newInstance(ParseUser.getCurrentUser());
                case USER_LIST:
                    return UserListFragment.newInstance(this.user);

            }
            return PlaceholderFragment.newInstance(0);
        }

        public int getMyListIndex() {
            return pagesList.indexOf(Pages.MY_LIST);
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return pagesList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //Spencer edit Kevin edit too + Nelson edit
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section4).toUpperCase(l);
            }
            return "Default Title";
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

}
