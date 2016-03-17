package com.applozic.mobicomkit.api;

import android.content.Context;
import android.util.Base64;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;

import com.applozic.mobicommons.commons.core.utils.Utils;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.util.TextUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by devashish on 27/12/14.
 */
public class MobiComKitClientService {

    protected Context context;
    public static final String BASE_URL_METADATA = "com.applozic.server.url";
    public static String APPLICATION_KEY_HEADER = "Application-Key";
    public static String APPLICATION_KEY_HEADER_VALUE_METADATA = "com.applozic.application.key";
    public static final String FILE_URL = "/rest/ws/aws/file/";
    protected String DEFAULT_URL = "https://apps.applozic.com";
    protected String FILE_BASE_URL = "https://applozic.appspot.com";


    public MobiComKitClientService() {

    }

    public MobiComKitClientService(Context context) {
        this.context = context;
    }

    protected String getBaseUrl() {
        String SELECTED_BASE_URL = MobiComUserPreference.getInstance(context).getUrl();

        if (!TextUtils.isEmpty(SELECTED_BASE_URL)) {
            return SELECTED_BASE_URL;
        }
        String BASE_URL = Utils.getMetaDataValue(context, BASE_URL_METADATA);
        if (!TextUtils.isEmpty(BASE_URL)) {
            return BASE_URL;
        }

        return DEFAULT_URL;
    }

    public UsernamePasswordCredentials getCredentials() {
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        if (!userPreferences.isRegistered()) {
            return null;
        }
        return new UsernamePasswordCredentials(userPreferences.getUserId(), userPreferences.getDeviceKeyString());
    }

    public HttpURLConnection openHttpConnection(String urlString) throws IOException {
        HttpURLConnection httpConn;

        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");

        try {
            httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            String userCredentials = getCredentials().getUserName() + ":" + getCredentials().getPassword();
            String basicAuth = "Basic " + Base64.encodeToString(userCredentials.getBytes(), Base64.NO_WRAP);
            httpConn.setRequestProperty("Authorization", basicAuth);
            httpConn.setRequestProperty(APPLICATION_KEY_HEADER, getApplicationKey(context));
            httpConn.setRequestProperty(HttpRequestUtils.USERID_HEADER, HttpRequestUtils.USERID_HEADER_VALUE);
            httpConn.connect();
            //Shifting this Code to individual class..this is needed so that caller can decide ..what should be done with the error
//            response = httpConn.getResponseCode();
//            if (response == HttpURLConnection.HTTP_OK) {
//                in = httpConn.getInputStream();
//
//            }

        } catch (Exception ex) {
            throw new IOException("Error connecting");
        }
        return httpConn;
    }

    public static String getApplicationKey(Context context) {

        return Utils.getMetaDataValue(context, APPLICATION_KEY_HEADER_VALUE_METADATA);

    }

    public String getFileUrl() {
        return FILE_BASE_URL + FILE_URL;
    }

}
