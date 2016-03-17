package com.applozic.mobicomkit;

import android.content.Context;
import android.content.SharedPreferences;

import com.applozic.mobicomkit.api.MobiComKitClientService;

/**
 * Created by devashish on 8/21/2015.
 */
public class ApplozicClient {

    private static final String HANDLE_DISPLAY_NAME = "CLIENT_HANDLE_DISPLAY_NAME";
    private static final String HANDLE_DIAL = "CLIENT_HANDLE_DIAL";
    public static ApplozicClient applozicClient;
    public SharedPreferences sharedPreferences;
    private Context context;

    private ApplozicClient(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(MobiComKitClientService.getApplicationKey(context), context.MODE_PRIVATE);
    }

    public static ApplozicClient getInstance(Context context) {
        if (applozicClient == null) {
            applozicClient = new ApplozicClient(context);
        }

        return applozicClient;
    }

    public boolean isHandleDisplayName() {
        return sharedPreferences.getBoolean(HANDLE_DISPLAY_NAME, true);
    }

    public ApplozicClient setHandleDisplayName(boolean enable) {
        sharedPreferences.edit().putBoolean(HANDLE_DISPLAY_NAME, enable).commit();
        return this;
    }

    public boolean isHandleDial() {
        return sharedPreferences.getBoolean(HANDLE_DIAL, false);
    }

    public ApplozicClient setHandleDial(boolean enable) {
        sharedPreferences.edit().putBoolean(HANDLE_DIAL, enable).commit();
        return this;
    }

}
