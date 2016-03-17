package com.applozic.mobicomkit.api.account.user;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.conversation.ApplozicMqttIntentService;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.database.MobiComDatabaseHelper;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Created by devashish on 24/12/14.
 */
public class UserClientService extends MobiComKitClientService {

    private static final String TAG = "UserClientService";
    public static final String SHARED_PREFERENCE_VERSION_UPDATE_KEY = "mck.version.update";
    public static final String PHONE_NUMBER_UPDATE_URL = "/rest/ws/registration/phone/number/update";
    public static final String NOTIFY_CONTACTS_ABOUT_JOINING_MT = "/rest/ws/registration/notify/contacts";
    public static final String VERIFICATION_CONTACT_NUMBER_URL = "/rest/ws/verification/number";
    public static final String VERIFICATION_CODE_CONTACT_NUMBER_URL = "/rest/ws/verification/code";
    public static final String APP_VERSION_UPDATE_URL = "/rest/ws/registration/version/update";
    public static final String SETTING_UPDATE_URL = "/rest/ws/setting/single/update";
    public static final String TIMEZONE_UPDATAE_URL = "/rest/ws/setting/updateTZ";
    public static final String USER_INFO_URL = "/rest/ws/user/info?";
    public static final Short MOBICOMKIT_VERSION_CODE = 105;

    private HttpRequestUtils httpRequestUtils;

    public UserClientService(Context context) {
        super(context);
        this.httpRequestUtils = new HttpRequestUtils(context);
    }

    public String getPhoneNumberUpdateUrl() {
        return getBaseUrl() + PHONE_NUMBER_UPDATE_URL;
    }

    public String getNotifyContactsAboutJoiningMt() {
        return getBaseUrl() + NOTIFY_CONTACTS_ABOUT_JOINING_MT;
    }

    public String getVerificationContactNumberUrl() {
        return getBaseUrl() + VERIFICATION_CONTACT_NUMBER_URL;
    }

    public String getVerificationCodeContactNumberUrl() {
        return getBaseUrl() + VERIFICATION_CODE_CONTACT_NUMBER_URL;
    }

    public String getAppVersionUpdateUrl() {
        return getBaseUrl() + APP_VERSION_UPDATE_URL;
    }

    public String getSettingUpdateUrl() {
        return getBaseUrl() + SETTING_UPDATE_URL;
    }

    public String getTimezoneUpdataeUrl() {
        return getBaseUrl() + TIMEZONE_UPDATAE_URL;
    }

    public String getUserInfoUrl() {
        return getBaseUrl() + USER_INFO_URL;
    }


    public void logout() {
        logout(false);
    }

    public void logout(boolean fromLogin) {
        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);
        final String userKeyString = mobiComUserPreference.getSuUserKeyString();
        String url = mobiComUserPreference.getUrl();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        mobiComUserPreference.clearAll();
        MessageDatabaseService.recentlyAddedMessage.clear();
        MobiComDatabaseHelper.getInstance(context).delDatabase();

        mobiComUserPreference.setUrl(url);

        if (!fromLogin) {
            Intent intent = new Intent(context, ApplozicMqttIntentService.class);
            intent.putExtra(ApplozicMqttIntentService.USER_KEY_STRING, userKeyString);
            context.startService(intent);
        }
    }

    public String updateTimezone(String osuUserKeyString) {
        //Note: This can be used if user decides to change the timezone
        String response = null;
        try {
            response = httpRequestUtils.getStringFromUrl(getTimezoneUpdataeUrl() + "?suUserKeyString=" + osuUserKeyString +
                    "&timeZone=" + URLEncoder.encode(TimeZone.getDefault().getID(), "UTF-8"));
            Log.i(TAG, "Response from sendDeviceTimezoneToServer : " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public boolean sendVerificationCodeToServer(String verificationCode) {
        try {
            String response = httpRequestUtils.getResponse(getCredentials(), getVerificationCodeContactNumberUrl() + "?verificationCode=" + verificationCode, "application/json", "application/json");
            JSONObject json = new JSONObject(response);
            return json.has("code") && json.get("code").equals("200");
        } catch (Exception e) {
            Log.e("Verification Code", "Got Exception while submitting verification code to server: " + e);
        }
        return false;
    }

    public void updateCodeVersion(final String deviceKeyString) {
                String url = getAppVersionUpdateUrl() + "?appVersionCode=" + MOBICOMKIT_VERSION_CODE + "&deviceKeyString=" + deviceKeyString;
                String response = httpRequestUtils.getResponse(getCredentials(), url, "text/plain", "text/plain");
                Log.i(TAG, "Version update response: " + response);

    }

    public String updatePhoneNumber(String contactNumber) throws UnsupportedEncodingException {
        return httpRequestUtils.getResponse(getCredentials(), getPhoneNumberUpdateUrl() + "?phoneNumber=" + URLEncoder.encode(contactNumber, "UTF-8"), "text/plain", "text/plain");
    }

    public void notifyFriendsAboutJoiningThePlatform() {
        String response = httpRequestUtils.getResponse(getCredentials(), getNotifyContactsAboutJoiningMt(), "text/plain", "text/plain");
        Log.i(TAG, "Response for notify contact about joining MT: " + response);
    }

    public String sendPhoneNumberForVerification(String contactNumber, String countryCode, boolean viaSms) {
        try {
            String viaSmsParam = "";
            if (viaSms) {
                viaSmsParam = "&viaSms=true";
            }
            return httpRequestUtils.getResponse(getCredentials(), getVerificationContactNumberUrl() + "?countryCode=" + countryCode + "&contactNumber=" + URLEncoder.encode(contactNumber, "UTF-8") + viaSmsParam, "application/json", "application/json");
        } catch (Exception e) {
            Log.e("Verification Code", "Got Exception while submitting contact number for verification to server: " + e);
        }
        return null;
    }

    public void updateSetting(final String key, final String value) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("key", key));
                    nameValuePairs.add(new BasicNameValuePair("value", value));
                    String response = httpRequestUtils.postData(getCredentials(), getSettingUpdateUrl(), "text/plain", "text/plain", null, nameValuePairs);
                    Log.i(TAG, "Response from setting update : " + response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public Map<String, String> getUserInfo(Set<String> userIds) throws JSONException, UnsupportedEncodingException {

        if (userIds == null && userIds.isEmpty()) {
            return new HashMap<>();
        }

        String userIdParam = "";
        for (String userId : userIds) {
            userIdParam += "&userIds" + "=" + URLEncoder.encode(userId, "UTF-8");
        }

        String response = httpRequestUtils.getResponse(getCredentials(), getUserInfoUrl() + userIdParam, "application/json", "application/json");
        Log.i(TAG, "Response: " + response);

        JSONObject jsonObject = new JSONObject(response);

        Map<String, String> info = new HashMap<String, String>();

        Iterator iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = jsonObject.getString(key);
            info.put(key, value);
        }
        return info;
    }
}
