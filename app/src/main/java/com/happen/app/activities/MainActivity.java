package com.happen.app.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.ViewOverlay;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.happen.app.R;
import com.happen.app.util.MyListCache;
import com.happen.app.util.NonSwipeableViewPager;
import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
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

    protected Menu myMenu;
    public enum Pages {FEED, FRIENDS, NEWS, MY_LIST};
    public Pages currentPage;
    private FeedFragment feedPage;
    private Fragment friendPage;
    private Fragment myListPage;
    private NewsFragment newsPage;
    private ViewGroup wrapperView;

    //@spencer used to self-identify in callback response...
    private MainActivity self;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Parse.initialize(this, "T67m6NTwHFuyyNavdRdFGlwNM5UiPE48l3sIP6fP", "GVaSbLvVYagIzZCd7XYLfG0H9lHJBwpUvsUKen7Z");
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        //for self-reference in call-backs
        self = this;

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
        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the friendsAdapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            /*.setText(mSectionsPagerAdapter.getPageTitle(i))*/
                            .setIcon(mSectionsPagerAdapter.getPageIcon(i, false, true))
                            .setTabListener(this));
        }
        currentPage = Pages.FEED;
        actionBar.setTitle(mSectionsPagerAdapter.getPageTitle(0));

        friendPage = null;
        myListPage = null;

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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.

        //if navigating away from news in tab browser, clear the green dots
        if(currentPage==Pages.NEWS)
        {
            newsPage.resetNewNews();
        }
        if(myMenu != null){
            tab.setIcon(mSectionsPagerAdapter.getPageIcon(tab.getPosition(),true,false));
            getActionBar().getTabAt(mViewPager.getCurrentItem()).setIcon(mSectionsPagerAdapter.getPageIcon(mViewPager.getCurrentItem(),false,false));
            getActionBar().setTitle(mSectionsPagerAdapter.getPageTitle(tab.getPosition()));
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
                    currentPage = Pages.NEWS;
                    switchMenuToAddEvent();
                    break;
                case 3:
                    currentPage = Pages.MY_LIST;
                    switchMenuToAddEvent();
                    break;
            }
        }

        mViewPager.setCurrentItem(tab.getPosition());

    }



    public void updateNewNews(int newNews)
    {
        if(newNews>0)
        {
            getActionBar().getTabAt(2).setIcon(mSectionsPagerAdapter.getPageIcon(newNews));
        }
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
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction  ){
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        if (currentPage == Pages.FEED) {
            feedPage.switchListToFeed();
        }
        if (currentPage == Pages.FRIENDS) {
            replaceFriendPage(null);
        }
        else if (currentPage == Pages.MY_LIST) {
            replaceMyListPage(null);
        }
    }

    @Override
    public void onBackPressed() {
        if (currentPage == Pages.FRIENDS && friendPage instanceof UserListFragment) {
            Log.d("MainActivity", "Going back to FriendsActivity");
            replaceFriendPage(null);
        }
        else if (currentPage == Pages.MY_LIST && myListPage instanceof EventDetailsFragment) {
            Log.d("MainActivity", "Going back to MyList");
            replaceMyListPage(null);
        }
        else {
            super.onBackPressed();
        }
    }

    public void replaceFriendPage(ParseUser targetUser) {
        // for when the user clicks the friend tab when in the requests view
        if (friendPage instanceof FriendsFragment && targetUser == null) {
            ((FriendsFragment) friendPage).switchListToFriends();
            return;
        }
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.remove(friendPage);
        ft.commit();
        if (friendPage instanceof FriendsFragment && targetUser != null) {
            friendPage = UserListFragment.newInstance(targetUser);
        }
        else {
            friendPage = FriendsFragment.newInstance(0);
        }
        mSectionsPagerAdapter.notifyDataSetChanged();
    }

    public void replaceMyListPage(String eventId)
    {
        if(myListPage instanceof MyListFragment && eventId == null)
        {
            return;
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.remove(myListPage);
        ft.commit();
        if (myListPage instanceof MyListFragment) {
            myListPage = EventDetailsFragment.newInstance(eventId);
        }
        else {
            MyListCache cache = MyListCache.getInstance();
            myListPage = cache.getMyListFragment();
        }
        mSectionsPagerAdapter.notifyDataSetChanged();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<Pages> pagesList;
        ParseUser user;
        MainActivity parent;

        FragmentManager mFragmentManager;

        public SectionsPagerAdapter(FragmentManager fm, MainActivity main) {
            super(fm);
            mFragmentManager = fm;
            init();
            this.parent = main;
        }

        public void init() {
            pagesList = new ArrayList<Pages>();
            pagesList.add(Pages.FEED);
            pagesList.add(Pages.FRIENDS);
            pagesList.add(Pages.NEWS);
            pagesList.add(Pages.MY_LIST);
        }

        public void setUser(ParseUser user) {
            this.user = user;
        }

        @Override
        public Fragment getItem(int position) {
            /*
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return translatePageEnum(pagesList.get(position));
            */

            switch (position) {
                // Feed Fragment
                case 0:
                    if (feedPage == null) {
                        feedPage = FeedFragment.newInstance(0);
                    }
                    return feedPage;

                // Handle special case (friend page)
                case 1:
                    if (friendPage == null) {
                        friendPage = FriendsFragment.newInstance(0);
                    }
                    return friendPage;

                // MyListFragment
                case 2:
                    if (newsPage == null){
                        newsPage = NewsFragment.newInstance(self);
                    }
                    return newsPage;

                // MyListFragment
                case 3:
                    if (myListPage == null){
                        myListPage = MyListFragment.newInstance();
                        MyListCache cache = MyListCache.getInstance();
                        cache.assignMyListFragment((MyListFragment) myListPage);
                    }
                    return myListPage;

                default:
                    // Should never reach here
                    Log.e("MainActivity", "Not sure which page we're in!");
                    return null;
            }
        }

        @Override
        public int getItemPosition(Object object) {
            if (object instanceof FriendsFragment && friendPage instanceof UserListFragment)
                return POSITION_NONE;
            if (object instanceof UserListFragment && friendPage instanceof FriendsFragment)
                return POSITION_NONE;
            if (object instanceof MyListFragment && myListPage instanceof EventDetailsFragment)
                return POSITION_NONE;
            if (object instanceof EventDetailsFragment && myListPage instanceof MyListFragment)
                return POSITION_NONE;
            return POSITION_UNCHANGED;
        }

        @Override
        public int getCount() {
            return pagesList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //Spencer edit Kevin edit too + Nelson edit
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return " "+getString(R.string.title_section1);
                case 1:
                    return " "+getString(R.string.title_section2);
                case 2:
                    return " "+getString(R.string.title_section3);
                case 3:
                    return " "+getString(R.string.title_section4);
            }
            return "Default Title";
        }

        public Drawable getPageIcon(int position, boolean isSelected, boolean isDefault, int numNotifications) {
            Locale l = Locale.getDefault();
            Drawable tabIcon = getResources().getDrawable(R.drawable.tab_feed);
            switch (position) {
                case 0:
                    if(isSelected || isDefault) {
                        tabIcon = getResources().getDrawable(R.drawable.tab_feed_hover);
                    } else {
                        tabIcon = getResources().getDrawable(R.drawable.tab_feed);
                    }
                    break;
                case 1:
                    if(isSelected) {
                        tabIcon = getResources().getDrawable(R.drawable.tab_friends_hover);
                    } else {
                        tabIcon = getResources().getDrawable(R.drawable.tab_friends);
                    }
                    break;
                case 2:
                    if(isSelected) {
                        tabIcon = getResources().getDrawable(R.drawable.tab_news_hover);
                    } else {
                        if(numNotifications > 0) {
                            switch(numNotifications) {
                                case 1:
                                    tabIcon = getResources().getDrawable(R.drawable.tab_news_1);
                                    break;
                                case 2:
                                    tabIcon = getResources().getDrawable(R.drawable.tab_news_2);
                                    break;
                                case 3:
                                    tabIcon = getResources().getDrawable(R.drawable.tab_news_3);
                                    break;
                                case 4:
                                    tabIcon = getResources().getDrawable(R.drawable.tab_news_4);
                                    break;
                                case 5:
                                    tabIcon = getResources().getDrawable(R.drawable.tab_news_5);
                                    break;
                                case 6:
                                    tabIcon = getResources().getDrawable(R.drawable.tab_news_6);
                                    break;
                                case 7:
                                    tabIcon = getResources().getDrawable(R.drawable.tab_news_7);
                                    break;
                                case 8:
                                    tabIcon = getResources().getDrawable(R.drawable.tab_news_8);
                                    break;
                                default:
                                    tabIcon = getResources().getDrawable(R.drawable.tab_news_9plus);
                                    break;
                            }
                        } else {
                            tabIcon = getResources().getDrawable(R.drawable.tab_news);
                        }
                    }
                    break;
                case 3:
                    if(isSelected) {
                        tabIcon = getResources().getDrawable(R.drawable.tab_mylist_hover);
                    } else {
                        tabIcon = getResources().getDrawable(R.drawable.tab_mylist);
                    }
                    break;
            }

            DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
            float densityScale = metrics.density;

            float scaledWidth = 25 * densityScale;
            float scaledHeight = 25 * densityScale;

            Bitmap bitmap = ((BitmapDrawable) tabIcon).getBitmap();
            Drawable d = new BitmapDrawable(getApplicationContext().getResources(),Bitmap.createScaledBitmap(bitmap, (int)scaledWidth, (int)scaledHeight, true));

            return d;
        }

        public Drawable getPageIcon(int position, boolean isSelected, boolean isDefault) {
            return getPageIcon(position, isSelected, isDefault, 0);
        }

        public Drawable getPageIcon(int numNotifications) {
            return getPageIcon(2, false, false, numNotifications);
        }
    }
}
