package com.happen.app.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.happen.app.R;
import com.happen.app.util.Util;

import java.io.ByteArrayOutputStream;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class SignupActivity extends Activity implements PopupMenu.OnMenuItemClickListener {

    /**
     * Static values for fetching images
     */
    private static final int SELECT_PICTURE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CROP_PICTURE = 2;

    // Percentage of profile picture width relative to screen size
    static final float WIDTH_RATIO = 0.25f; // 25%

    // Values for email and password at the time of the signup attempt.
    private String mPassword;
    private String mConfirmPassword;
    private String mUsername;
    private String mPhone;
    private String mFirstName;
    private String mLastName;
    private Bitmap mImage;

    // UI references.
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private EditText mUsernameView;
    private EditText mPhoneView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private ImageView mImageView;

    private View mSignupFormView;
    private View mSignupStatusView;
    private TextView mSignupStatusMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setLogo(R.drawable.logo);

        // Set up the signup form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mUsernameView.setText(mUsername);

        mPhoneView = (EditText) findViewById(R.id.phone);
        mPhoneView.setText(mPhone);

        mFirstNameView = (EditText) findViewById(R.id.fname);
        mFirstNameView.setText(mFirstName);

        mLastNameView = (EditText) findViewById(R.id.lname);
        mLastNameView.setText(mLastName);

        mConfirmPasswordView = (EditText) findViewById(R.id.password2);
        mConfirmPasswordView.setText(mConfirmPassword);

        mImageView = (ImageView) findViewById(R.id.profile_picture);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.defaultprofile);

        // Get screen dimensions and calculate desired profile picture size
        Display display = this.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        bitmap = Util.circularCrop(bitmap, (int) (width * WIDTH_RATIO / 2));
        mImageView.setImageBitmap(bitmap);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptSignup();
                    return true;
                }
                return false;
            }
        });

        mSignupFormView = findViewById(R.id.login_form);
        mSignupStatusView = findViewById(R.id.login_status);
        mSignupStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignup();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.signup, menu);
        return true;
    }

    // KEVIN: added function to bring up popup when selecting profile picture
    public void changePhoto(View v){
        PopupMenu popup = new PopupMenu(this, v);
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
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // if image capture was successful save to bitmap
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Display display = this.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageBitmap = Util.resizeToScale(imageBitmap);
            imageBitmap = Util.circularCrop(imageBitmap, (int) (width * WIDTH_RATIO / 2));
            mImageView.setImageBitmap(imageBitmap);
            mImage = imageBitmap;
        }
        // if gallery selection was successful save to bitmap
        else if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            cropCapturedImage(imageUri);
        }
        else if (requestCode == CROP_PICTURE && resultCode == RESULT_OK) {
            Display display = this.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageBitmap = Util.circularCrop(imageBitmap, (int) (width * WIDTH_RATIO / 2));
            mImageView.setImageBitmap(imageBitmap);
            mImage = imageBitmap;
        }
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
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptSignup() {

        // Reset errors.
        mUsernameView.setError(null);
        mFirstNameView.setError(null);
        mLastNameView.setError(null);
        mPhoneView.setError(null);
        mPasswordView.setError(null);
        mConfirmPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mPassword = mPasswordView.getText().toString();
        mConfirmPassword = mConfirmPasswordView.getText().toString();
        mUsername = mUsernameView.getText().toString();
        mFirstName = mFirstNameView.getText().toString();
        mLastName = mLastNameView.getText().toString();
        mPhone = mPhoneView.getText().toString().replaceAll("[()-]", "");

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        } else if (TextUtils.isEmpty(mConfirmPassword)) {
            mConfirmPasswordView.setError(getString(R.string.error_field_required));
            focusView = mConfirmPasswordView;
            cancel = true;
        } else if (!mPassword.equals(mConfirmPassword)) {
            mConfirmPasswordView.setError(getString(R.string.error_mismatch_password));
            focusView = mConfirmPasswordView;
            cancel = true;
        }

        // Check for a valid phone number
        if (TextUtils.isEmpty(mPhone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        } else if (mPhone.length() < 10) {
            mPhoneView.setError(getString(R.string.error_invalid_number));
            focusView = mPhoneView;
            cancel = true;
        } else {
            try {
                double number = Double.parseDouble(mPhone);
            } catch (NumberFormatException nfe) {
                mPhoneView.setError(getString(R.string.error_invalid_number));
                focusView = mPhoneView;
                cancel = true;
            }
        }

        // Check for completion of other fields
        if (TextUtils.isEmpty(mLastName)) {
            mLastNameView.setError(getString(R.string.error_field_required));
            focusView = mLastNameView;
            cancel = true;
        }
        if (TextUtils.isEmpty(mFirstName)) {
            mFirstNameView.setError(getString(R.string.error_field_required));
            focusView = mFirstNameView;
            cancel = true;
        }
        if (TextUtils.isEmpty(mUsername)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            Parse.initialize(this, "T67m6NTwHFuyyNavdRdFGlwNM5UiPE48l3sIP6fP", "GVaSbLvVYagIzZCd7XYLfG0H9lHJBwpUvsUKen7Z");
            createUser(mFirstName, mLastName, mUsername, mPassword, mPhone);
        }
    }

    public void createUser(String firstname, String lastname, String username, String password, String phone) {

        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        // other fields can be set just like with ParseObject
        user.put("firstName", firstname);
        user.put("lastName", lastname);
        user.put("phoneNumber", phone);

        // add profile picture if it exists
        if(mImage != null){
            // create Parse file to store image
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            mImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] array = stream.toByteArray();
            ParseFile file = new ParseFile("profile.png", array);
            try {
                file.save();
                // store image in profile attribute of Parse user
                user.put("profilePic", file);
            } catch (ParseException e) {
                System.out.println(e);
                mUsernameView.setError(getString(R.string.error_upload_photo));
                mUsernameView.requestFocus();
            }
        }
        // create user on Parse
        createParseUser(user);
    }

    public void createParseUser(ParseUser user){
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Hooray! Let them use the app now.
                    System.out.println("succeeded!");
                    // Show a progress spinner, and kick off a background task to
                    // perform the user login attempt.
                    mSignupStatusMessageView.setText(R.string.signup_progress_signing_up);
                    showProgress(true);
                    Intent i = new Intent(SignupActivity.this, SplashscreenActivity.class);
                    startActivity(i);
                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                    System.out.println(e);
                    mUsernameView.setError(getString(R.string.error_username_taken));
                    mUsernameView.requestFocus();
                }
            }
        });
    }

    protected void onPostExecute(final Boolean success) {
        showProgress(false);
        if (success) {
            Intent i = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(i);
        } else {
            mUsernameView.setError(getString(R.string.error_failed_signup));
            mUsernameView.requestFocus();
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mSignupStatusView.setVisibility(View.VISIBLE);
            mSignupStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mSignupStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mSignupFormView.setVisibility(View.VISIBLE);
            mSignupFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mSignupFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mSignupStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mSignupFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
