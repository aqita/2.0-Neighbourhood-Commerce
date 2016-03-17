package com.aqitapartners.sellers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.UserClientService;
import com.applozic.mobicomkit.api.conversation.ApplozicMqttIntentService;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.MessageCommunicator;
import com.applozic.mobicomkit.uiwidgets.conversation.MobiComKitBroadcastReceiver;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComKitActivityInterface;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class HomeActivity extends ActionBarActivity implements NavigationView.OnNavigationItemSelectedListener, MessageCommunicator, MobiComKitActivityInterface, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final int LOCATION_SERVICE_ENABLE = 1001;
    public static final String TAKE_ORDER = "takeOrder";
    public static final String CONTACT = "contact";
    protected static final long UPDATE_INTERVAL = 5;
    protected static final long FASTEST_INTERVAL = 1;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final String CAPTURED_IMAGE_URI = "capturedImageUri";
    private static Uri capturedImageUri;
    private static final String SHARE_TEXT = "share_text";
    private static String inviteMessage;
    protected ConversationFragment conversation;
    protected MobiComQuickConversationFragment quickConversationFragment;
    protected MobiComKitBroadcastReceiver mobiComKitBroadcastReceiver;
    protected ActionBar mActionBar;
    boolean isChatFrag = false;
    protected GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Contact contact;
    
    private static int retry ;
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


    public static void addFragment(FragmentActivity fragmentActivity, Fragment fragmentToAdd, String fragmentTag) {
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();

        // Fragment activeFragment = UIService.getActiveFragment(fragmentActivity);
        FragmentTransaction fragmentTransaction = supportFragmentManager
                .beginTransaction();
        fragmentTransaction.replace(R.id.home_container, fragmentToAdd,
                fragmentTag);

        if (supportFragmentManager.getBackStackEntryCount() > 1) {
            supportFragmentManager.popBackStack();
        }
        fragmentTransaction.addToBackStack(fragmentTag);
      /*if (activeFragment != null) {
            fragmentTransaction.hide(activeFragment);
        }*/
        fragmentTransaction.commit();
        supportFragmentManager.executePendingTransactions();
        //Log.i(TAG, "BackStackEntryCount: " + supportFragmentManager.getBackStackEntryCount());
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mobiComKitBroadcastReceiver, BroadcastService.getIntentFilter());

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
        super.onPause();
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

    FloatingActionButton fab ;
    Context c ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        ((ActionBarActivity) this).onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext() , EditOffer.class );
                intent.putExtra( EditOffer.KEY_IS_EDIT_MODE , false ) ;
                UserInfoHandler handler = UserInfoHandler.getInstance(c) ;
                if ( handler.getType().equals("s") )
                {
                    intent.putExtra( EditOffer.KEY_ITEM_HASUOM , false ) ;
                }
                else
                {
                    intent.putExtra( EditOffer.KEY_ITEM_HASUOM , true ) ;
                }

                startActivityForResult(intent, 786);
            }
        });

        View header = navigationView.inflateHeaderView(R.layout.nav_header_home) ;
        TextView tv = (TextView) header.findViewById(R.id.name);
        if (tv != null )
        {
            UserInfoHandler handler = UserInfoHandler.getInstance(this) ;
            tv.setText( handler.getName() );
        }

        RoundedImageView iv = (RoundedImageView) header.findViewById(R.id.imageView) ;
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext() , InfoActivity.class ) ;
                startActivity(intent);
            }
        });

        UserInfoHandler handler = UserInfoHandler.getInstance(c) ;
        if ( handler.getType().equals("s") )
        {
            Menu menu = navigationView.getMenu();
            MenuItem item = menu.findItem(R.id.services);
            item.setTitle("Services") ;
        }
        else if (handler.getType().equals("p"))
        {
            Menu menu = navigationView.getMenu();
            MenuItem item = menu.findItem(R.id.services);
            item.setTitle("Products") ;
        }
        else
        {
            Menu menu = navigationView.getMenu();
            menu.removeItem(R.id.services);
        }


        if (URLUtil.isValidUrl(handler.getImageUrl()))
        {
            try{
                String url = handler.getImageUrl();
                url = "http://thesocialmediamonthly.com/wp-content/uploads/2015/08/photo.png" ;
                URLConnection conn = (new URL(url)).openConnection();
                InputStream stream = conn.getInputStream();
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true ;
                Bitmap downloadedImage = BitmapFactory.decodeStream(stream , null , opts);

                opts.inJustDecodeBounds = false ;
                downloadedImage = BitmapFactory.decodeStream(stream , null , opts);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (savedInstanceState == null)
        {
//            getFragmentManager().beginTransaction().add(R.id.home_container, new ChatFragment()) ;
        }


        mActionBar = getSupportActionBar();
        inviteMessage = Utils.getMetaDataValue(getApplicationContext(), SHARE_TEXT);
        if (savedInstanceState != null && !TextUtils.isEmpty(savedInstanceState.getString(CAPTURED_IMAGE_URI))) {
            capturedImageUri = Uri.parse(savedInstanceState.getString(CAPTURED_IMAGE_URI));
            contact = (Contact) savedInstanceState.getSerializable(CONTACT);
            conversation = new ConversationFragment(contact);
            addFragment(this, conversation, ConversationUIService.CONVERSATION_FRAGMENT);
        } else {
            quickConversationFragment = new MobiComQuickConversationFragment();
//            addFragment(this, quickConversationFragment, ConversationUIService.QUICK_CONVERSATION_FRAGMENT);
            getSupportFragmentManager().beginTransaction().replace(R.id.home_container, quickConversationFragment, ConversationUIService.QUICK_CONVERSATION_FRAGMENT).commit() ;
            boolean isChatFrag = true;

//            quickConversationFragment = new MobiComQuickConversationFragment();
//            addFragment(this, quickConversationFragment, ConversationUIService.QUICK_CONVERSATION_FRAGMENT);
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
//        mActionBar.setTitle(R.string.conversations);
        /*getSupportActionBar().setTitle(R.string.conversations);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);*/

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
//        onNewIntent(getIntent());



        new MobiComConversationService(this).processLastSeenAtStatus();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {

            if (!isChatFrag)
            {
                quickConversationFragment = new MobiComQuickConversationFragment();
//            addFragment(this, quickConversationFragment, ConversationUIService.QUICK_CONVERSATION_FRAGMENT);
                getSupportFragmentManager().beginTransaction().replace(R.id.home_container, quickConversationFragment, ConversationUIService.QUICK_CONVERSATION_FRAGMENT).commit() ;
                isChatFrag = true ;
            } else {
                Boolean takeOrder = getIntent().getBooleanExtra(TAKE_ORDER, false);
                if (takeOrder)
                    this.finish();
                else
                    super.onBackPressed();

            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.getMenuInflater().inflate( R.menu.mobicom_basic_menu_for_normal_message, menu);
        menu.removeItem(R.id.start_new);
        menu.findItem(R.id.dial).setVisible(false);

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.chats) {
            fab.setVisibility(View.VISIBLE);
            fab.hide();
            mActionBar.setTitle("Home");

            quickConversationFragment = new MobiComQuickConversationFragment();
//            addFragment(this, quickConversationFragment, ConversationUIService.QUICK_CONVERSATION_FRAGMENT);
            getSupportFragmentManager().beginTransaction().replace(R.id.home_container, quickConversationFragment, ConversationUIService.QUICK_CONVERSATION_FRAGMENT).commit() ;
            isChatFrag = true ;

//            Intent intent = new Intent(this, ConversationActivity.class);
//            startActivity(intent);
        } else if (id == R.id.services  ) {
            // Handle the camera action
            UserInfoHandler handler = UserInfoHandler.getInstance(c) ;
            if ( handler.getType().equals("s") )
            {
                mActionBar.setTitle("Your Services");
            }
            else
            {
                mActionBar.setTitle("Your Products");
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.home_container, new ServicesFragment() , ServicesFragment.SERVICE_FRAGMENT_TAG ).commit() ;
            getSupportFragmentManager().executePendingTransactions();
            fab.show();
            isChatFrag = false ;
        } else if (id == R.id.terms) {
            fab.hide();
//            fab.hide();
            getSupportFragmentManager().beginTransaction().replace(R.id.home_container, new TermsFragment()).commit();
            mActionBar.setTitle("Terms and Conditions");
            isChatFrag = false ;
        } else if (id == R.id.about)
        {
            fab.hide();
            mActionBar.setTitle("About Us");
            getSupportFragmentManager().beginTransaction().replace(R.id.home_container , new AboutFragment()).commit();
            isChatFrag = false ;
        } else if (id == R.id.logout) {
            fab.setVisibility(View.VISIBLE);
            UserInfoHandler handler = UserInfoHandler.getInstance(this);
            handler.setIsNotAuthenticated();
            isChatFrag = false ;

            new UserClientService(this).logout();

            Intent intent = new Intent();
            intent.setClass(getApplicationContext() , LoginActivity.class);
            startActivity(intent);
            finish();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        new ConversationUIService(this).checkForStartNewConversation(getIntent());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        new ConversationUIService(this).onActivityResult(requestCode, resultCode, data);
        System.out.println("OnActivityResult Activity : " + requestCode);

        if (requestCode == LOCATION_SERVICE_ENABLE) {
            if (((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                googleApiClient.connect();
            } else {
                Toast.makeText(HomeActivity.this, R.string.unable_to_fetch_location, Toast.LENGTH_LONG).show();
            }
            return;
        }
        else if (requestCode == 786)
        {
            System.out.println("OnActivityResult Activity");
            if (data !=null)
            {
                String newName = data.getStringExtra(EditOffer.RETURN_NAME);
                String newQty = data.getStringExtra(EditOffer.RETURN_QTY);
                String newUOM = data.getStringExtra(EditOffer.RETURN_UOM);
                String newPrice = data.getStringExtra(EditOffer.RETURN_PRICE);
                String newId = data.getStringExtra(EditOffer.RETURN_ID);
                boolean newIsUOMAvailable = data.getBooleanExtra(EditOffer.RETURN_HASUOM , false) ;

                ServiceListItem item = new ServiceListItem() ;
                item.setName(newName);
                item.setId(newId);
                item.setPrice(newPrice);
                item.setUom(newUOM);
                item.setIsUOMAvailable(newIsUOMAvailable);

                System.out.println("Looking for fragment");
                ServicesFragment frag = (ServicesFragment) getSupportFragmentManager().findFragmentByTag(ServicesFragment.SERVICE_FRAGMENT_TAG) ;
                if (frag != null)
                {
                    System.out.println("Fragment found");
                    frag.addOrUpdateItem(item);
                    frag.refreshListView();
                }
            }
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
                            Toast.makeText(HomeActivity.this, R.string.location_sending_cancelled, Toast.LENGTH_LONG).show();
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
    public void updateLatestMessage(Message message, String formattedContactNumber) {
        new ConversationUIService(this).updateLatestMessage(message, formattedContactNumber);

    }

    @Override
    public void removeConversation(Message message, String formattedContactNumber) {
        new ConversationUIService(this).removeConversation(message, formattedContactNumber);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (mCurrentLocation == null) {
            Toast.makeText(this, R.string.waiting_for_current_location, Toast.LENGTH_SHORT).show();
            locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(UPDATE_INTERVAL);
            locationRequest.setFastestInterval(FASTEST_INTERVAL);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
        if (mCurrentLocation != null) {
            conversation.attachLocation(mCurrentLocation);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(((Object) this).getClass().getSimpleName(), "onConnectionSuspended() called.");
    }

    @Override
    public void onLocationChanged(Location location) {

        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        conversation.attachLocation(location);
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
        GooglePlayServicesUtil.getErrorDialog(code, this, CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
    }

    public Contact getContact() {
        return contact;
    }

    public static Uri getCapturedImageUri() {
        return capturedImageUri;
    }

    public static void setCapturedImageUri(Uri capturedImageUri) {
        HomeActivity.capturedImageUri = capturedImageUri;
    }

}
