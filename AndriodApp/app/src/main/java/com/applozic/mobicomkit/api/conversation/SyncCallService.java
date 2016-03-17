package com.applozic.mobicomkit.api.conversation;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;

import java.util.Date;
import java.util.List;
/**
 * Created by applozic on 12/2/15.
 */
public class SyncCallService {

    private static final String TAG = "SyncCall";

    private static SyncCallService syncCallService;
    private MobiComMessageService mobiComMessageService;
    private MobiComConversationService mobiComConversationService;
    private BaseContactService contactService;

    private SyncCallService(Context context) {
        this.mobiComMessageService = new MobiComMessageService(context, MessageIntentService.class);
        this.mobiComConversationService = new MobiComConversationService(context);
        this.contactService = new AppContactService(context);
    }

    public synchronized static SyncCallService getInstance(Context context) {
        if (syncCallService == null) {
            syncCallService = new SyncCallService(context);
        }
        return syncCallService;
    }


    public synchronized void updateDeliveryStatus(String key) {
        mobiComMessageService.updateDeliveryStatus(key);
    }

    public synchronized List<Message> getLatestMessagesGroupByPeople(){
        return mobiComConversationService.getLatestMessagesGroupByPeople(null);
    }

    public synchronized List<Message> getLatestMessagesGroupByPeople(Long createdAt){
        return mobiComConversationService.getLatestMessagesGroupByPeople(createdAt);
    }

    public synchronized void syncMessages(String key) {
        if (!TextUtils.isEmpty(key) && mobiComMessageService.isMessagePresent(key)) {
            Log.d(TAG, "Message is already present, MQTT reached before GCM.");
        } else {
            mobiComMessageService.syncMessages();
        }
    }

    public synchronized void updateDeliveryStatusForContact(String contactId) {
        mobiComMessageService.updateDeliveryStatusForContact(contactId);
    }

    public synchronized void updateConnectedStatus(String contactId, Date date, boolean connected) {
        contactService.updateConnectedStatus(contactId, date, connected);
    }

}
