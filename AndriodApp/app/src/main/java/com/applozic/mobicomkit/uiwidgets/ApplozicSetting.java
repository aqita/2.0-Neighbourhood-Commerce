package com.applozic.mobicomkit.uiwidgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import com.aqitapartners.sellers.R;

import com.applozic.mobicomkit.api.MobiComKitClientService;

/**
 * Created by devashish on 8/21/2015.
 */
public class ApplozicSetting {

    private Context context;
    public SharedPreferences sharedPreferences;
    private static final String START_NEW_FLOATING_ACTION_BUTTON_DISPLAY = "SETTING_START_NEW_FLOATING_ACTION_BUTTON_DISPLAY";
    private static final String START_NEW_BUTTON_DISPLAY = "SETTING_START_NEW_BUTTON_DISPLAY";
    private static final String NO_CONVERSATION_LABEL = "SETTING_NO_CONVERSATION_LABEL";
    private static final String CONVERSATION_CONTACT_IMAGE_VISIBILITY = "CONVERSATION_CONTACT_IMAGE_VISIBILITY";
    private static final String SENT_MESSAGE_BACKGROUND_COLOR = "SENT_MESSAGE_BACKGROUND_COLOR";
    private static final String RECEIVED_MESSAGE_BACKGROUND_COLOR = "RECEIVED_MESSAGE_BACKGROUND_COLOR";
    private static final String ONLINE_STATUS_MASTER_LIST = "ONLINE_STATUS_MASTER_LIST";
    private static final String PRICE_WIDGET = "PRICE_WIDGET";


    public static ApplozicSetting applozicSetting;

    private ApplozicSetting(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(MobiComKitClientService.getApplicationKey(context), context.MODE_PRIVATE);
    }

    public static ApplozicSetting getInstance(Context context) {
        if (applozicSetting == null) {
            applozicSetting = new ApplozicSetting(context);
        }

        return applozicSetting;
    }

    public ApplozicSetting setSentMessageBackgroundColor(int color) {
        sharedPreferences.edit().putInt(SENT_MESSAGE_BACKGROUND_COLOR, color).commit();
        return this;
    }

    public ApplozicSetting setReceivedMessageBackgroundColor(int color) {
        sharedPreferences.edit().putInt(RECEIVED_MESSAGE_BACKGROUND_COLOR, color).commit();
        return this;
    }

    public int getSentMessageBackgroundColor() {
        return sharedPreferences.getInt(SENT_MESSAGE_BACKGROUND_COLOR, Color.WHITE);
    }

    public int getReceivedMessageBackgroundColor() {
        return sharedPreferences.getInt(RECEIVED_MESSAGE_BACKGROUND_COLOR, Color.WHITE);
    }

    public ApplozicSetting showOnlineStatusInMasterList() {
        sharedPreferences.edit().putBoolean(ONLINE_STATUS_MASTER_LIST, true).commit();
        return this;
    }

    public ApplozicSetting hideOnlineStatusInMasterList() {
        sharedPreferences.edit().putBoolean(ONLINE_STATUS_MASTER_LIST, false).commit();
        return this;
    }

    public boolean isOnlineStatusInMasterListVisible() {
        return sharedPreferences.getBoolean(ONLINE_STATUS_MASTER_LIST, false);
    }

    public ApplozicSetting showConversationContactImage() {
        sharedPreferences.edit().putBoolean(CONVERSATION_CONTACT_IMAGE_VISIBILITY, true).commit();
        return this;
    }

    public ApplozicSetting hideConversationContactImage() {
        sharedPreferences.edit().putBoolean(CONVERSATION_CONTACT_IMAGE_VISIBILITY, false).commit();
        return this;
    }

    public boolean isConversationContactImageVisible() {
        return sharedPreferences.getBoolean(CONVERSATION_CONTACT_IMAGE_VISIBILITY, true);
    }

    public ApplozicSetting showStartNewButton() {
        sharedPreferences.edit().putBoolean(START_NEW_BUTTON_DISPLAY, true).commit();
        return this;
    }

    public ApplozicSetting hideStartNewButton() {
        sharedPreferences.edit().putBoolean(START_NEW_BUTTON_DISPLAY, false).commit();
        return this;
    }

    public boolean isStartNewButtonVisible() {
        return sharedPreferences.getBoolean(START_NEW_BUTTON_DISPLAY, false);
    }

    public ApplozicSetting showStartNewFloatingActionButton() {
        sharedPreferences.edit().putBoolean(START_NEW_FLOATING_ACTION_BUTTON_DISPLAY, true).commit();
        return this;
    }

    public ApplozicSetting hideStartNewFloatingActionButton() {
        sharedPreferences.edit().putBoolean(START_NEW_FLOATING_ACTION_BUTTON_DISPLAY, false).commit();
        return this;
    }

    public boolean isStartNewFloatingActionButtonVisible() {
        return sharedPreferences.getBoolean(START_NEW_BUTTON_DISPLAY, false);
    }

    public String getNoConversationLabel() {
        return sharedPreferences.getString(NO_CONVERSATION_LABEL, context.getResources().getString(R.string.no_conversation));
    }

    public ApplozicSetting setNoConversationLabel(String label) {
        sharedPreferences.edit().putString(NO_CONVERSATION_LABEL, label).commit();
        return this;
    }

    public ApplozicSetting showPriceOption() {
        sharedPreferences.edit().putBoolean(PRICE_WIDGET, true).commit();
        return this;
    }

    public ApplozicSetting hidePriceOption() {
        sharedPreferences.edit().putBoolean(PRICE_WIDGET, false).commit();
        return this;
    }

    public boolean isPriceOptionVisible() {
        return sharedPreferences.getBoolean(PRICE_WIDGET, false);
    }

    public boolean clearAll() {
        return sharedPreferences.edit().clear().commit();
    }

}