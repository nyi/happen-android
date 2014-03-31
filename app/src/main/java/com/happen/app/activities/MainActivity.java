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
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;
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
    public enum Pages {FEED, FRIENDS, MY_LIST};
    public Pages currentPage;
    private Fragment friendPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        friendPage = null;
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
        if(id == R.id.action_news)
        {
            ListPopupWindow popup = new ListPopupWindow(this);

            popup.show();
            System.out.println("news has been clicked.");
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
        mViewPager.setCurrentItem(tab.getPosition());

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
    }

    public void replaceFriendPage(ParseUser targetUser) {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.remove(friendPage);
        ft.commit();
        if (friendPage instanceof FriendsFragment) {
            friendPage = UserListFragment.newInstance(targetUser);
        }
        else {
            friendPage = FriendsFragment.newInstance(0);
        }
        mSectionsPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (currentPage == Pages.FRIENDS && friendPage instanceof UserListFragment) {
            Log.d("MainActivity", "Going back to FriendsActivity");
            replaceFriendPage(null);
        }
        else {
            super.onBackPressed();
        }
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


        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;
            init();
        }

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
                    return FeedFragment.newInstance(0);

                // Handle special case (friend page)
                case 1:
                    if (friendPage == null) {
                        friendPage = FriendsFragment.newInstance(0);
                    }
                    return friendPage;

                // MyListFragment
                case 2:
                    return MyListFragment.newInstance(0);

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
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section4).toUpperCase(l);
            }
            return "Default Title";
        }
    }


    public class NewsObject
    {

    }
}
