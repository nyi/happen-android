package com.happen.app.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.happen.app.R;
import com.happen.app.components.UserListAdapter;
import com.happen.app.util.Util;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Kevin on 2/10/14.
 */
public class MyListFragment extends Fragment implements View.OnClickListener, PopupMenu.OnMenuItemClickListener{
    // XML node keys
    static final String KEY_EMPTY = "empty";
    static final String KEY_EVENT_DETAILS = "eventDetails";
    static final String KEY_PROFILE_PIC = "profilePic";
    static final String KEY_FIRSTNAME = "firstName";
    static final String KEY_LASTNAME = "lastName";

    // Parse column names
    static final String TABLE_USER = "User";
    static final String TABLE_EVENT = "Event";
    static final String COL_CREATOR = "creator";
    static final String COL_DETAILS = "details";
    static final String COL_CREATED_AT = "createdAt";

    static final int SELECT_PICTURE = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int CROP_PICTURE = 2;

    // Percentage of profile picture width relative to screen size
    static final float WIDTH_RATIO = 0.25f; // 25%

    UserListAdapter adapter;
    ImageView imageView;
    TextView nameView, handleView;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the MyList fragment layout
        View v = inflater.inflate(R.layout.fragment_mylist, container, false);

        // Get current user
        final ParseUser user = ParseUser.getCurrentUser();

        // Set up profile picture, full name and user handle
        imageView = (ImageView)v.findViewById(R.id.mylist_picture);
        imageView.setOnClickListener(this);
        nameView = (TextView)v.findViewById(R.id.mylist_fullname);
        handleView = (TextView)v.findViewById(R.id.mylist_username);

        // Set full name and user handle
        nameView.setText(user.getString(KEY_FIRSTNAME) + " " + user.getString((KEY_LASTNAME)));
        handleView.setText("@" + user.getUsername());

        ParseFile parsePic = (ParseFile)user.get(KEY_PROFILE_PIC);
        if (parsePic == null) {
            Log.e("MyListFragment", "Failed to create ParseFile object");

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
                            Log.e("MyListFragment", "Received invalid byte array for profile picture.");
                        else {
                            Bitmap bitmap;
                            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            // Get screen dimensions and calculate desired profile picture size
                            Display display = getActivity().getWindowManager().getDefaultDisplay();
                            Point size = new Point();
                            display.getSize(size);
                            int width = size.x;

                            bitmap = Util.circularCrop(bitmap, (int) (width * WIDTH_RATIO / 2));
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                    else {
                        Log.e("MyListFragment", "Failed to retrieve profile picture for user " + user.getUsername());
                        Log.e("MyListFragment", "Error: " + e.getMessage());

                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.defaultprofile);

                        // Get screen dimensions and calculate desired profile picture size
                        Display display = getActivity().getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);
                        int width = size.x;

                        bitmap = Util.circularCrop(bitmap, (int) (width * WIDTH_RATIO / 2));
                        imageView.setImageBitmap(bitmap);
                    }
                }
            });
        }

        // Set up event list
        ListView listview = (ListView)v.findViewById(R.id.mylist_eventlist);
        ArrayList<HashMap<String,String>> eventsList = new ArrayList<HashMap<String,String>>();
        adapter = new UserListAdapter(eventsList, inflater);
        listview.setAdapter(adapter);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE_EVENT);
        query.include(COL_CREATOR);
        query.whereEqualTo(COL_CREATOR, ParseObject.createWithoutData("_" + TABLE_USER, user.getObjectId()));
        query.orderByDescending(COL_CREATED_AT);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.d("MyListFragment", "Retrieved " + object.size() + " scores");
                    ArrayList<HashMap<String, String>> eventsList = new ArrayList<HashMap<String, String>>();
                    if(object.size() == 0) { // User has not created any events yet
                        HashMap<String, String> event = new HashMap<String, String>();
                        event.put(KEY_EMPTY, "You have no events. You should create one!");
                        eventsList.add(event);
                    } else {
                        for (int i = 0; i < object.size(); i++) {
                            HashMap<String, String> event = new HashMap<String, String>();
                            if(object.get(i).has(COL_DETAILS)) {
                                event.put(KEY_EVENT_DETAILS, object.get(i).getString(COL_DETAILS));
                            }
                            eventsList.add(event);
                        }
                    }

                    adapter.replace(eventsList);
                } else {
                    Log.d("MyListFragment", "Error: " + e.getMessage());
                }
            }
        });

        return v;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    // KEVIN: added function to bring up popup when selecting profile picture
    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.mylist_picture:
                changePhoto(v);
                break;
        }
    }

    public void changePhoto(View view){
        PopupMenu popup = new PopupMenu(getActivity(), view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.photo);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.take_photo:
                takePhoto();
                return true;
            case R.id.upload_photo:
                uploadPhoto();
                return true;
            default:
                return false;
        }
    }

    public void takePhoto(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void uploadPhoto(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
    }

    public void cropCapturedImage(Uri imageUri){
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri of image
            cropIntent.setDataAndType(imageUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 200);
            cropIntent.putExtra("outputY", 200);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, CROP_PICTURE);
        } catch (ActivityNotFoundException e) {
            String errorMessage = "Your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this.getActivity(), errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap image = null;
        // if image capture was successful save to bitmap
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageBitmap = Util.resizeToScale(imageBitmap);
            image = imageBitmap;
        }
        // if gallery selection was successful save to bitmap
        else if (requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = data.getData();
            cropCapturedImage(imageUri);
        }
        else if (requestCode == CROP_PICTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            image = imageBitmap;
        }
        if(image != null){
            // update view to display new profile picture
            Display display = this.getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            Bitmap circularImage = Util.circularCrop(image, (int) (width * WIDTH_RATIO / 2));
            imageView.setImageBitmap(circularImage);

            // update Parse and replace old picture with new upload
            ParseUser user = ParseUser.getCurrentUser();
            // create Parse file to store image
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] array = stream.toByteArray();
            ParseFile file = new ParseFile("profile.png", array);
            try {
                file.save();
                // store image in profile attribute of Parse user
                user.put("profilePic", file);
                user.saveInBackground();
            } catch (ParseException e) {
                System.out.println(e);
            }

        }
    }
}
