package com.applozic.mobicomkit.uiwidgets.conversation.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.ApplozicMqttService;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.conversation.ApplozicMqttIntentService;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageIntentService;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.api.conversation.MobiComMessageService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.uiwidgets.ApplozicSetting;
import com.applozic.mobicomkit.uiwidgets.conversation.UIService;
import com.aqitapartners.sellers.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.MessageCommunicator;
import com.applozic.mobicomkit.uiwidgets.conversation.MobiComKitBroadcastReceiver;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.ConversationFragment;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MobiComQuickConversationFragment;
import com.applozic.mobicomkit.uiwidgets.instruction.InstructionUtil;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.contact.Contact;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


/**
 * Created by devashish on 6/25/2015.
 */

public class ConversationActivity extends ActionBarActivity implements MessageCommunicator, MobiComKitActivityInterface, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final int LOCATION_SERVICE_ENABLE = 1001;
    public static final String TAKE_ORDER = "takeOrder";
    public static final String CONTACT = "contact";
    protected static final long UPDATE_INTERVAL = 5;
    protected static final long FASTEST_INTERVAL = 1;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final String CAPTURED_IMAGE_URI = "capturedImageUri";
    private static final String SHARE_TEXT = "share_text";
    private static Uri capturedImageUri;
    private static String inviteMessage;
    protected ConversationFragment conversation;
    protected MobiComQuickConversationFragment quickConversationFragment;
    protected MobiComKitBroadcastReceiver mobiComKitBroadcastReceiver;
    protected ActionBar mActionBar;
    protected GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Contact contact;
    private static int retry ;

    public ConversationActivity() {

    }

    public Snackbar snackbar;

    @Override
    public void showErrorMessageView(String message) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.footerAd);
        layout.setVisibility(View.VISIBLE);
        snackbar = Snackbar.make(layout, message, Snackbar.LENGTH_LONG);
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.setDuration(Snackbar.LENGTH_LONG);
        ViewGroup group = (ViewGroup) snackbar.getView();
        TextView textView = (TextView) group.findViewById(R.id.snackbar_action);
        textView.setTextColor(Color.YELLOW);
        group.setBackgroundColor(getResources().getColor(R.color.error_background_color));
        TextView txtView = (TextView) group.findViewById(R.id.snackbar_text);
        txtView.setMaxLines(5);
        snackbar.show();
    }

    @Override
    public void retry() {
        retry++;
    }

    @Override
    public int getRetryCount() {
        return retry;
    }
    public void dismissErrorMessage() {
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }

    public static void addFragment(FragmentActivity fragmentActivity, Fragment fragmentToAdd, String fragmentTag) {
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();

        //Fragment activeFragment = UIService.getActiveFragment(fragmentActivity);
        FragmentTransaction fragmentTransaction = supportFragmentManager
                .beginTransaction();
        fragmentTransaction.replace(R.id.layout_child_activity, fragmentToAdd,
                fragmentTag);

        if (supportFragmentManager.getBackStackEntryCount() > 1) {
            supportFragmentManager.popBackStack();
        }
        fragmentTransaction.addToBackStack(fragmentTag);

/*
        if (activeFragment != null) {
            fragmentTransaction.hide(activeFragment);
        }
*/

        fragmentTransaction.commit();
        supportFragmentManager.executePendingTransactions();
        //Log.i(TAG, "BackStackEntryCount: " + supportFragmentManager.getBackStackEntryCount());
    }

    @Override
    protected void onStop() {
        super.onStop();
        final String userKeyString = MobiComUserPreference.getInstance(this).getSuUserKeyString();
        Intent intent = new Intent(this, ApplozicMqttIntentService.class);
        intent.putExtra(ApplozicMqttIntentService.USER_KEY_STRING, userKeyString);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mobiComKitBroadcastReceiver, BroadcastService.getIntentFilter());
        Intent subscribeIntent = new Intent(this, ApplozicMqttIntentService.class);
        subscribeIntent.putExtra(ApplozicMqttIntentService.SUBSCRIBE, true);
        startService(subscribeIntent);

        if (!Utils.isInternetAvailable(this)) {
            String errorMessage = getResources().getString(R.string.internet_connection_not_available);
            showErrorMessageView(errorMessage);
        }
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mobiComKitBroadcastReceiver);
        //ApplozicMqttService.getInstance(this).unSubscribe();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (capturedImageUri != null) {
            savedInstanceState.putString(CAPTURED_IMAGE_URI, capturedImageUri.toString());
            savedInstanceState.putSerializable(CONTACT, contact);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            Utils.toggleSoftKeyBoard(this, true);
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quickconversion_activity);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        mActionBar = getSupportActionBar();
        inviteMessage = Utils.getMetaDataValue(getApplicationContext(), SHARE_TEXT);
        retry = 0;
        if (savedInstanceState != null && !TextUtils.isEmpty(savedInstanceState.getString(CAPTURED_IMAGE_URI))) {
            capturedImageUri = Uri.parse(savedInstanceState.getString(CAPTURED_IMAGE_URI));
            contact = (Contact) savedInstanceState.getSerializable(CONTACT);
            conversation = new ConversationFragment(contact);
            addFragment(this, conversation, ConversationUIService.CONVERSATION_FRAGMENT);
        } else {
            quickConversationFragment = new MobiComQuickConversationFragment();
            addFragment(this, quickConversationFragment, ConversationUIService.QUICK_CONVERSATION_FRAGMENT);
        }

        mobiComKitBroadcastReceiver = new MobiComKitBroadcastReceiver(this);
        InstructionUtil.showInfo(this, R.string.info_message_sync, BroadcastService.INTENT_ACTIONS.INSTRUCTION.toString());

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) finish();
            }
        });
        mActionBar.setTitle(R.string.conversations);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        onNewIntent(getIntent());

        new MobiComConversationService(this).processLastSeenAtStatus();
        //ApplozicMqttService.getInstance(this).subscribe();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //setIntent(intent);
        if (!MobiComUserPreference.getInstance(this).isLoggedIn()) {
            //user is not logged in
            Log.i("AL", "user is not logged in yet.");
            return;
        }

        try {
            new ConversationUIService(this).checkForStartNewConversation(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showActionBar() {
        mActionBar.setDisplayShowTitleEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        showActionBar();
        //return false;
        getMenuInflater().inflate(R.menu.mobicom_basic_menu_for_normal_message, menu);
        if (!ApplozicSetting.getInstance(this).isStartNewButtonVisible()) {
            menu.removeItem(R.id.start_new);
        }
        if (!ApplozicClient.getInstance(this).isHandleDial()) {
            menu.findItem(R.id.dial).setVisible(false);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        new ConversationUIService(this).onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_SERVICE_ENABLE) {
            if (((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                googleApiClient.connect();
            } else {
                Toast.makeText(ConversationActivity.this, R.string.unable_to_fetch_location, Toast.LENGTH_LONG).show();
            }
            return;
        }
    }

    public void processLocation() {
        if (!((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.location_services_disabled_title)
                    .setMessage(R.string.location_services_disabled_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.location_service_settings, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, LOCATION_SERVICE_ENABLE);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            Toast.makeText(ConversationActivity.this, R.string.location_sending_cancelled, Toast.LENGTH_LONG).show();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            googleApiClient.disconnect();
            googleApiClient.connect();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.start_new) {
            new ConversationUIService(this).startContactActivityForResult();
        } else if (id == R.id.refresh) {
            String message = this.getString(R.string.info_message_sync);
            new MobiComMessageService(this, MessageIntentService.class).syncMessagesWithServer(message);
        } else if (id == R.id.shareOptions) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setAction(Intent.ACTION_SEND)
                    .setType("text/plain").putExtra(Intent.EXTRA_TEXT, inviteMessage);
            startActivity(Intent.createChooser(intent, "Share Via"));
            return super.onOptionsItemSelected(item);
        } else if (id == R.id.deleteConversation) {
            if(conversation != null){
                conversation.deleteConversationThread();
            }
        }
        return false;
    }

    @Override
    public void onQuickConversationFragmentItemClick(View view, Contact contact) {
        conversation = new ConversationFragment(contact);
        addFragment(this, conversation, ConversationUIService.CONVERSATION_FRAGMENT);
        this.contact = contact;
    }

    @Override
    public void startContactActivityForResult() {
        new ConversationUIService(this).startContactActivityForResult();
    }

    @Override
    public void addFragment(ConversationFragment conversationFragment) {
        addFragment(this, conversationFragment, ConversationUIService.CONVERSATION_FRAGMENT);
        conversation = conversationFragment;
    }

    @Override
    public void onBackPressed() {
        Boolean takeOrder = getIntent().getBooleanExtra(TAKE_ORDER, false);
        if (takeOrder)
            this.finish();
        else
            super.onBackPressed();
    }

    @Override
    public void updateLatestMessage(Message message, String formattedContactNumber) {
        new ConversationUIService(this).updateLatestMessage(message, formattedContactNumber);

    }

    @Override
    public void removeConversation(Message message, String formattedContactNumber) {
        new ConversationUIService(this).removeConversation(message, formattedContactNumber);
    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
            Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (mCurrentLocation == null) {
                Toast.makeText(this, R.string.waiting_for_current_location, Toast.LENGTH_SHORT).show();
                locationRequest = new LocationRequest();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(UPDATE_INTERVAL);
                locationRequest.setFastestInterval(FASTEST_INTERVAL);
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            }
            if (mCurrentLocation != null && conversation != null) {
                conversation.attachLocation(mCurrentLocation);
            }
        } catch (Exception e) {
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(((Object) this).getClass().getSimpleName(),
                "onConnectionSuspended() called.");

    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            if (conversation != null && location != null) {
                conversation.attachLocation(location);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }

    }

    void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this,
                CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
    }

    public Contact getContact() {
        return contact;
    }

    public static Uri getCapturedImageUri() {
        return capturedImageUri;
    }

    public static void setCapturedImageUri(Uri capturedImageUri) {
        ConversationActivity.capturedImageUri = capturedImageUri;
    }

}

