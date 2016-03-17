package com.applozic.mobicomkit.api.conversation;

import android.app.IntentService;
import android.content.Intent;
import android.os.Process;
import android.text.TextUtils;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicommons.people.contact.Contact;

/**
 * Created by sunil on 26/12/15.
 */
public class ApplozicIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public static final String PAIRED_MESSAGE_KEY_STRING = "pairedMessageKey";
    public static final String CONTACT = "contact";
    private static final String TAG = "ApplozicIntentService";

    public ApplozicIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String pairedMessageKeyString = intent.getStringExtra(PAIRED_MESSAGE_KEY_STRING);
        final Contact contact = (Contact) intent.getSerializableExtra(CONTACT);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                final MessageClientService messageClientService = new MessageClientService(getApplicationContext());

                if (!TextUtils.isEmpty(pairedMessageKeyString)) {
                    messageClientService.updateReadStatusForSingleMessage(pairedMessageKeyString);
                }
                if (contact != null) {
                    int read = new MessageDatabaseService(getApplicationContext()).updateReadStatus(contact.getContactIds());

                    if (read > 0) {
                        messageClientService.updateReadStatus(contact);
                    }
                }
            }
        });
        thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

    }
}

