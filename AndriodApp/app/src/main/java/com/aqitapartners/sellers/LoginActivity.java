package com.aqitapartners.sellers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.account.user.UserLoginTask.TaskListener ;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import communication.SyncGet;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    Context c ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserInfoHandler handler = UserInfoHandler.getInstance(this);
        if (handler.getIsAuthenticated() )
        {
            startHomeActivity();
            finish();
        }

        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        c = this ;

        ActionBar actionBar = getSupportActionBar() ;
        actionBar.setBackgroundDrawable( new ColorDrawable(Color.parseColor("#f8981d") ));

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isPhoneValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

/*
    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }
*/

    private boolean isPhoneValid(String phone) {
        //TODO: Replace this with your own logic
        return true ;
//        return phone.length() == 10 ;

    }



    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return true ;
//        return password.length() > 4;
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.

                Thread.sleep(2);

                SyncGet get = new SyncGet();
                get.setUseCase(SyncGet.CASE_VERIFY_URL_SLUG);

                List<Pair<String, String >> paramsForService = new ArrayList<Pair<String , String>>();
                paramsForService.add( new Pair<String,String>("phone" , mEmail) ) ;
                paramsForService.add( new Pair<String,String>("password" , mPassword) ) ;

                get.setParams(paramsForService);

                if ( get.execute() )
                {
                    String response = get.getResponse();

                    try {
                        JSONObject jObj = new JSONObject(response);

                        System.out.print("JSON Object" + jObj.toString());

                        if ( !jObj.getBoolean("authenticated") )
                        {
                            return false ;
                        }
                        else {

                            int id = jObj.getInt("storeId") ;
                            String name = jObj.getString("name");
                            String type = jObj.getString("type");
//                            String url = jObj.getString("image") ;
//                            boolean isUrlValid = false;
//                            if (URLUtil.isValidUrl(url))
//                            {
//                                isUrlValid = true ;
//                            }

                            UserInfoHandler handler = UserInfoHandler.getInstance(c);

                            handler.setIsAuthenticated();
                            handler.setStoreId(id);
                            handler.setName(name);
                            handler.setType(type);

/*                          This stuff goes into the onPostExecute method since this is allowed only on UI Thread.
                            GCMRegistrationUtils gcmRegistrationUtils = new GCMRegistrationUtils(LoginActivity.this);
                            gcmRegistrationUtils.setUpGcmNotification();
*/


//                            if (isUrlValid)
//                            {
//                                handler.setImageUrl(url) ;
//                            }

                        }

/*
                        SyncGet getUserIfo = new SyncGet();
                        getUserIfo.setUseCase(SyncGet.CASE_FETCH_USER_INFO);
                        List<Pair<String,String>> paramsForUserInfo = new ArrayList< Pair<String,String>>();
                        getUserIfo.setParams(paramsForUserInfo);


                        if (getUserIfo.execute())
                        {
                            JSONObject jUserInfo = new JSONObject(getUserIfo.getResponse());

                            System.out.print("JSON Object" + jUserInfo.toString());

                            if ( !jUserInfo.getBoolean("authenticated") )
                            {
                                return false ;
                            }
                            else {

                                */
/**
                                 *
                                 * {
                                 "storeId": 201,
                                 "name": "Store1",
                                 "tags": "grocery | fruits \u0026amp vegetables | fresh",
                                 "area": "Ulsoor",
                                 "phone": "9999999999",
                                 "timing": "10.30 pm to 4.30 pm, 7 days open",
                                 "description": "store 1 is the best",
                                 "servicearea": "Ulsoor, Indranagar, Cox Town",
                                 "image": "www.image.jpg",
                                 "rating": "4.2",
                                 "address": "10/7, store 1 road, ulsoor",
                                 "homedelivery": false
                                 * }
                                 *
                                 *
                                 *//*


                                int id = jUserInfo.getInt("storeId") ;
                                String name = jUserInfo.getString("name");
                                String type = jUserInfo.getString("type");
                                String tags = jUserInfo.getString("tags");
                                String area = jUserInfo.getString("area");
                                String phone = jUserInfo.getString("phone");
                                String timing = jUserInfo.getString("timing");
                                String description = jUserInfo.getString("description");
                                String servicearea = jUserInfo.getString("servicearea");
                                String image = jUserInfo.getString("image");
                                String rating = jUserInfo.getString("rating");
                                String address = jUserInfo.getString("address");
                                boolean isDeliveryAvailable = jUserInfo.getBoolean("homedelivery");


                                UserInfoHandler handler = UserInfoHandler.getInstance(c);

                                handler.setIsAuthenticated();
                                handler.setStoreId(id);
                                handler.setName(name);
                                handler.setType(type);
                                handler.setTags(tags);
                                handler.setArea(area);
                                handler.setPhone(phone);
                                handler.setTiming(timing);
                                handler.setDescription(description);
                                handler.setServiceArea(servicearea);
                                handler.setImageUrl(image);
                                handler.setRating(rating);
                                handler.setAddress(address);
                                handler.setIsDeliveryAvailable(isDeliveryAvailable);

                            }
                        }
*/



                        return true ;

                    } catch (JSONException e) {
                        e.printStackTrace();
                        return false ;
                    }
                }
                else
                {
                    return false ;
                }



            } catch (InterruptedException e) {
                return false;
            }

/*
            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
*/
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {

                com.applozic.mobicomkit.api.account.user.UserLoginTask.TaskListener listener = new TaskListener() {

                    @Override
                    public void onSuccess(RegistrationResponse registrationResponse, Context context) {

                        GCMRegistrationUtils gcmRegistrationUtils = new GCMRegistrationUtils(LoginActivity.this);
                        gcmRegistrationUtils.setUpGcmNotification();

                        System.out.println("Chat Login Successful");
                        System.out.println(registrationResponse.getUserKey());
                    }

                    @Override
                    public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                        System.out.println("Chat login failed") ;
                        exception.printStackTrace();

                    }
                };


                User user = new User();
                UserInfoHandler handler = UserInfoHandler.getInstance(c);
                user.setUserId( ""+ handler.getStoreId());
                user.setDisplayName( "" + handler.getName() );

//              user.setEmail(email); //optional

                new com.applozic.mobicomkit.api.account.user.UserLoginTask(user, listener, c).execute((Void) null);

                startHomeActivity();
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void startHomeActivity()
    {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext() , HomeActivity.class) ;
        startActivity(intent);
    }

}

