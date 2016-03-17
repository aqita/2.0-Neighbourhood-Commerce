package com.applozic.mobicomkit.api.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.api.conversation.SyncCallService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.feed.GcmMessageResponse;
import com.applozic.mobicomkit.feed.MqttMessageResponse;
import com.applozic.mobicommons.json.GsonUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class MobiComPushReceiver {

    public static final String MTCOM_PREFIX = "APPLOZIC_";
    public static final List<String> notificationKeyList = new ArrayList<String>();
    private static final String TAG = "MobiComPushReceiver";
    private static Queue<String> notificationIdList = new LinkedList<String>();

    static {

        notificationKeyList.add("APPLOZIC_01"); // 0 for MESSAGE_RECEIVED //done
        notificationKeyList.add("APPLOZIC_02");// 1 for MESSAGE_SENT
        notificationKeyList.add("APPLOZIC_03");// 2 for MESSAGE_SENT_UPDATE
        notificationKeyList.add("APPLOZIC_04"); //3 for MESSAGE_DELIVERED//done
        notificationKeyList.add("APPLOZIC_05"); //4 for MESSAGE_DELETED
        notificationKeyList.add("APPLOZIC_06");// 5 for CONVERSATION_DELETED//done
        notificationKeyList.add("APPLOZIC_07"); // 6 for MESSAGE_READ
        notificationKeyList.add("APPLOZIC_08"); // 7 for MESSAGE_DELIVERED_AND_READ//done
        notificationKeyList.add("APPLOZIC_09"); // 8 for CONVERSATION_READ
        notificationKeyList.add("APPLOZIC_10"); // 9 for CONVERSATION_DELIVERED_AND_READ
        notificationKeyList.add("APPLOZIC_11");// 10 for USER_CONNECTED//done
        notificationKeyList.add("APPLOZIC_12");// 11 for USER_DISCONNECTED//done
        notificationKeyList.add("APPLOZIC_13");// 12 for GROUP_DELETED
        notificationKeyList.add("APPLOZIC_14");// 13 for GROUP_LEFT

       /* notificationKeyList.add("MT_SYNC"); // 0
        notificationKeyList.add("MT_MARK_ALL_MESSAGE_AS_READ"); //1
         notificationKeyList.add("MT_DELIVERED"); //2
        notificationKeyList.add("MT_SYNC_PENDING"); //3
         notificationKeyList.add("MT_DELETE_MESSAGE"); //4
        notificationKeyList.add("MT_DELETE_MULTIPLE_MESSAGE"); //5
         notificationKeyList.add("MT_DELETE_MESSAGE_CONTACT");// 6
        notificationKeyList.add("MTEXTER_USER");//7
        notificationKeyList.add("MT_CONTACT_VERIFIED"); //8
        notificationKeyList.add("MT_CONTACT_UPDATED"); //9
        notificationKeyList.add("MT_DEVICE_CONTACT_SYNC");//10
        notificationKeyList.add("MT_EMAIL_VERIFIED");//11
        notificationKeyList.add("MT_DEVICE_CONTACT_MESSAGE");//12
        notificationKeyList.add("MT_CANCEL_CALL");//13
        notificationKeyList.add("MT_MESSAGE");//14
        notificationKeyList.add("MT_USER_CONNECTED");//15
        notificationKeyList.add("MT_USER_DISCONNECTED");//16
        notificationKeyList.add("MT_MESSAGE_DELIVERED_READ"); //17 MESSAGE_DELIVERED_READ
        notificationKeyList.add("MT_CONVERSATION_READ"); //18*/
    }

    public static boolean isMobiComPushNotification(Intent intent) {
        Log.i(TAG, "checking for Applozic notification.");
        return isMobiComPushNotification(intent.getExtras());
    }

    public static boolean isMobiComPushNotification(Bundle bundle) {
        //This is to identify collapse key sent in notification..
        String payLoad = bundle.getString("collapse_key");
        Log.i(TAG, "Received notification: " + payLoad);

        if (payLoad != null && payLoad.contains(MTCOM_PREFIX) || notificationKeyList.contains(payLoad)) {
            return true;
        } else {
            for (String key : notificationKeyList) {
                payLoad = bundle.getString(key);
                if (payLoad != null) {
                    return true;
                }
            }
            return false;
        }
    }

    public synchronized static boolean processPushNotificationId(String id) {
        if (id != null && notificationIdList != null && notificationIdList.contains(id)) {
            if(notificationIdList.size()>0){
                notificationIdList.remove(id);
            }
            return true;
        }
        return false;
    }

    public synchronized static void addPushNotificationId(String notificationId) {

        try {
            if (notificationIdList != null && notificationIdList.size() < 20) {
                notificationIdList.add(notificationId);
            }
            if (notificationIdList != null && notificationIdList.size() == 20) {
                for (int i = 1; i <= 14; i++) {
                    if(notificationIdList.size()>0){
                        notificationIdList.remove();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void processMessage(Context context, Bundle bundle) {
        // Bundle extras = intent.getExtras();
        if (bundle != null) {
            // ToDo: do something for invalidkey ;
            // && extras.get("InvalidKey") != null
            String message = bundle.getString("collapse_key");

            /*
            "key" : "APPLOZIC_01",
            "value" : "{sadjflkjalsdfj}
            MqttResponse
            * */

            String deleteConversationForContact = bundle.getString(notificationKeyList.get(5));
            String deleteMessage = bundle.getString(notificationKeyList.get(4));
            //  String multipleMessageDelete = bundle.getString(notificationKeyList.get(5));
            // String mtexterUser = bundle.getString(notificationKeyList.get(7));
            String payloadForDelivered = bundle.getString(notificationKeyList.get(3));
            if (TextUtils.isEmpty(payloadForDelivered)) {
                payloadForDelivered = bundle.getString(notificationKeyList.get(7));
            }
            String userConnected = bundle.getString(notificationKeyList.get(11));
            String userDisconnected = bundle.getString(notificationKeyList.get(12));
            processMessage(context, bundle, message, deleteConversationForContact, deleteMessage, payloadForDelivered, userConnected, userDisconnected);
        }
    }

    public static void processMessage(final Context context, Bundle bundle, String message, String deleteConversationForContact, String deleteMessage, String payloadForDelivered, String userConnected, String userDisconnected) {
        SyncCallService syncCallService = SyncCallService.getInstance(context);
        try {
            if (!TextUtils.isEmpty(payloadForDelivered)) {
                MqttMessageResponse messageResponseForDelivered = (MqttMessageResponse) GsonUtils.getObjectFromJson(payloadForDelivered, MqttMessageResponse.class);
                if (processPushNotificationId(messageResponseForDelivered.getId())) {
                    return;
                }
                addPushNotificationId(messageResponseForDelivered.getId());
                String splitKeyString[] = (messageResponseForDelivered.getMessage()).toString().split(",");
                String keyString = splitKeyString[0];
                String userId = splitKeyString[1];
                syncCallService.updateDeliveryStatus(keyString);
            }

            if (!TextUtils.isEmpty(deleteConversationForContact)) {
                MqttMessageResponse deleteConversationResponse = (MqttMessageResponse) GsonUtils.getObjectFromJson(deleteConversationForContact, MqttMessageResponse.class);
                if (processPushNotificationId(deleteConversationResponse.getId())) {
                    return;
                }
                addPushNotificationId(deleteConversationResponse.getId());
                MobiComConversationService conversationService = new MobiComConversationService(context);
                conversationService.deleteConversationFromDevice(deleteConversationResponse.getMessage().toString());
                BroadcastService.sendConversationDeleteBroadcast(context, BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString(), deleteConversationResponse.getMessage().toString(), "success");
            }

        /*if (!TextUtils.isEmpty(mtexterUser)) {
            Log.i(TAG, "Received GCM message MTEXTER_USER: " + mtexterUser);
            if (mtexterUser.contains("{")) {
                Gson gson = new Gson();
                ContactContent contactContent = gson.fromJson(mtexterUser, ContactContent.class);
                ContactService.addUsersToContact(context, contactContent.getContactNumber(), contactContent.getAppVersion(), true);
            } else {
                String[] details = mtexterUser.split(",");
                ContactService.addUsersToContact(context, details[0], Short.parseShort(details[1]), true);
            }
        }*/

            if (!TextUtils.isEmpty(userConnected)) {
                MqttMessageResponse userConnectedResponse = (MqttMessageResponse) GsonUtils.getObjectFromJson(userConnected, MqttMessageResponse.class);
                if (processPushNotificationId(userConnectedResponse.getId())) {
                    return;
                }
                addPushNotificationId(userConnectedResponse.getId());
                syncCallService.updateConnectedStatus(userConnectedResponse.getMessage().toString(), new Date(), true);
            /*final String userId = userConnected;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    UserDetail[] userDetail = messageClientService.getUserDetails(userId);
                    if (userDetail != null) {
                        for (UserDetail userDetails : userDetail) {
                            if (userDetails != null && userDetails.isConnected()) {
                                new ContactDatabase(context).updateConnectedOrDisconnectedStatus(userId, userDetails.isConnected());
                            }
                        }
                    }
                }
            }).start();*/
            }

            if (!TextUtils.isEmpty(userDisconnected)) {
                MqttMessageResponse userDisconnectedResponse = (MqttMessageResponse) GsonUtils.getObjectFromJson(userConnected, MqttMessageResponse.class);
                if (processPushNotificationId(userDisconnectedResponse.getId())) {
                    return;
                }
                addPushNotificationId(userDisconnectedResponse.getId());
                String[] parts = userDisconnectedResponse.getMessage().toString().split(",");
                String userId = parts[0];
                Date lastSeenAt = new Date();
                if (parts.length >= 2) {
                    lastSeenAt = new Date(Long.valueOf(parts[1]));
                }
                syncCallService.updateConnectedStatus(userId, lastSeenAt, false);
            /*final String userId = userDisconnected;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    UserDetail[] userDetail = messageClientService.getUserDetails(userId);
                    if (userDetail != null) {
                        for (UserDetail userDetails : userDetail) {
                            if (userDetails != null && userDetails.getLastSeenAtTime() != null) {
                                ContactDatabase contactDatabase = new ContactDatabase(context);
                                contactDatabase.updateConnectedOrDisconnectedStatus(userId, userDetails.isConnected());
                                contactDatabase.updateLastSeenTimeAt(userId, userDetails.getLastSeenAtTime());
                            }
                        }
                    }
                }
            }).start();*/
            }

      /*  if (!TextUtils.isEmpty(multipleMessageDelete)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            MessageDeleteContent messageDeleteContent = gson.fromJson(multipleMessageDelete, MessageDeleteContent.class);

            for (String deletedSmsKeyString : messageDeleteContent.getDeleteKeyStrings()) {
                processDeleteSingleMessageRequest(context, deletedSmsKeyString, messageDeleteContent.getContactNumber());
            }
        }*/

            if (!TextUtils.isEmpty(deleteMessage)) {
                MqttMessageResponse deleteSingleMessageResponse = (MqttMessageResponse) GsonUtils.getObjectFromJson(deleteMessage, MqttMessageResponse.class);
                if (processPushNotificationId(deleteSingleMessageResponse.getId())) {
                    return;
                }
                addPushNotificationId(deleteSingleMessageResponse.getId());
                String deleteMessageKeyAndUserId = deleteSingleMessageResponse.getMessage().toString();
                String contactNumbers = deleteMessageKeyAndUserId.split(",").length > 1 ? deleteMessageKeyAndUserId.split(",")[1] : null;
                processDeleteSingleMessageRequest(context, deleteMessageKeyAndUserId.split(",")[0], contactNumbers);
            }

            String messageSent = bundle.getString(notificationKeyList.get(1));
            if (!TextUtils.isEmpty(messageSent)) {
                GcmMessageResponse syncSentMessageResponse = (GcmMessageResponse) GsonUtils.getObjectFromJson(messageSent, GcmMessageResponse.class);
                if (processPushNotificationId(syncSentMessageResponse.getId())) {
                    return;
                }
                addPushNotificationId(syncSentMessageResponse.getId());
                syncCallService.syncMessages(null);
            }

            String messageKey = bundle.getString(notificationKeyList.get(0));
            GcmMessageResponse syncMessageResponse = null;
            if (!TextUtils.isEmpty(messageKey)) {
                syncMessageResponse = (GcmMessageResponse) GsonUtils.getObjectFromJson(messageKey, GcmMessageResponse.class);
                if (processPushNotificationId(syncMessageResponse.getId())) {
                    return;
                }
                addPushNotificationId(syncMessageResponse.getId());
                Message messageObj = syncMessageResponse.getMessage();
                if (!TextUtils.isEmpty(messageObj.getKeyString())) {
                    syncCallService.syncMessages(messageObj.getKeyString());
                } else {
                    syncCallService.syncMessages(null);
                }

            }

           /* if (notificationKeyList.get(1).equalsIgnoreCase(message)) {

            } else if (messageObj != null && messageObj.getKeyString() != null && !TextUtils.isEmpty(messageObj.getKeyString())) {
                Log.i(TAG, "MT sync for key: " + messageObj.getKeyString());
                syncCallService.syncMessages(messageObj.getKeyString());
            } else if (syncMessageResponse != null && notificationKeyList.get(0).equalsIgnoreCase(syncMessageResponse.getId())) {
                syncCallService.syncMessages(null);
            } else if (notificationKeyList.get(3).equalsIgnoreCase(message)) {
                //  MessageStatUtil.sendMessageStatsToServer(context);
            }*/
            String conversationReadResponse = bundle.getString(notificationKeyList.get(9));
            if (!TextUtils.isEmpty(conversationReadResponse)) {
                MqttMessageResponse updateDeliveryStatusForContactResponse = (MqttMessageResponse) GsonUtils.getObjectFromJson(conversationReadResponse, MqttMessageResponse.class);
                if (notificationKeyList.get(9).equals(updateDeliveryStatusForContactResponse.getType())) {
                    if (processPushNotificationId(updateDeliveryStatusForContactResponse.getId())) {
                        return;
                    }
                    addPushNotificationId(updateDeliveryStatusForContactResponse.getId());
                    syncCallService.updateDeliveryStatusForContact(updateDeliveryStatusForContactResponse.getMessage().toString());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void processDeleteSingleMessageRequest(Context context, String deletedSmsKeyString, String contactNumber) {
        MobiComConversationService conversationService = new MobiComConversationService(context);
        contactNumber = conversationService.deleteMessageFromDevice(deletedSmsKeyString, contactNumber);
        BroadcastService.sendMessageDeleteBroadcast(context, BroadcastService.INTENT_ACTIONS.DELETE_MESSAGE.toString(), deletedSmsKeyString, contactNumber);
    }

    public static void processMessageAsync(final Context context, final Bundle bundle) {
        if (MobiComUserPreference.getInstance(context).isLoggedIn()) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    processMessage(context, bundle);
                }
            }).start();
        }
    }

    public static void processMessageAsync(final Context context, final Intent intent) {
        processMessageAsync(context, intent.getExtras());
    }

}
