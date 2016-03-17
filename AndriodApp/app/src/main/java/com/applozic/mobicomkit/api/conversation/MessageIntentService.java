package com.applozic.mobicomkit.api.conversation;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.conversation.schedule.MessageSenderTimerTask;
import com.applozic.mobicomkit.api.conversation.schedule.ScheduleMessageService;

import com.applozic.mobicommons.json.GsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

/**
 * Created by devashish on 15/12/13.
 */
public class MessageIntentService extends IntentService {

    private static final String TAG = "MessageIntentService";

    public MessageIntentService() {
        super("MessageIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Message message = (Message) GsonUtils.getObjectFromJson(intent.getStringExtra(MobiComKitConstants.MESSAGE_JSON_INTENT), Message.class);
        Thread thread = new Thread(new MessegeSender(message));
        thread.start();
    }

    private class MessegeSender implements Runnable {
        private Message message;

        public MessegeSender(Message message) {
            this.message = message;
        }

        @Override
        public void run() {
            try {
                new MessageClientService(MessageIntentService.this).sendMessageToServer(message, ScheduleMessageService.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
