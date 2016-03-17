package com.applozic.mobicomkit.contact.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.database.MobiComDatabaseHelper;

import com.applozic.mobicommons.people.contact.Contact;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by adarsh on 9/7/15.
 */
public class ContactDatabase {

    public static final String CONTACT = "contact";
    private static final String TAG = "ContactDatabaseService";
    Context context = null;
    private MobiComUserPreference userPreferences;
    private MobiComDatabaseHelper dbHelper;

    public ContactDatabase(Context context) {
        this.context = context;
        this.userPreferences = MobiComUserPreference.getInstance(context);
        this.dbHelper = MobiComDatabaseHelper.getInstance(context);
    }

    /**
     * Form a single contact from cursor
     *
     * @param cursor
     * @return
     */
    public Contact getContact(Cursor cursor) {
        Contact contact = new Contact();
        contact.setFullName(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.FULL_NAME)));
        contact.setUserId(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.USERID)));
        contact.setLocalImageUrl(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.CONTACT_IMAGE_LOCAL_URI)));
        contact.setImageURL(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.CONTACT_IMAGE_URL)));
        String contactNumber = cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.CONTACT_NO));
        if (TextUtils.isEmpty(contactNumber)) {
            contact.setContactNumber(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.USERID)));
        } else {
            contact.setContactNumber(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.CONTACT_NO)));
        }
        contact.setApplicationId(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.APPLICATION_ID)));
        Long connected = cursor.getLong(cursor.getColumnIndex(MobiComDatabaseHelper.CONNECTED));
        contact.setConnected(connected != 0 && connected.intValue() == 1);
        contact.setLastSeenAt(cursor.getLong(cursor.getColumnIndex(MobiComDatabaseHelper.LAST_SEEN_AT_TIME)));
        contact.processContactNumbers(context);
        return contact;
    }

    /**
     * Form a single contact details from cursor
     *
     * @param cursor
     * @return
     */
    public List<Contact> getContactList(Cursor cursor) {

        List<Contact> smsList = new ArrayList<Contact>();
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            do {
                smsList.add(getContact(cursor));
            } while (cursor.moveToNext());
        }
        return smsList;
    }

    public List<Contact> getAllContact() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(CONTACT, null, null, null, null, null, MobiComDatabaseHelper.FULL_NAME + " asc");
        List<Contact> contactList = getContactList(cursor);
        cursor.close();
        dbHelper.close();
        return contactList;
    }

    public Contact getContactById(String id) {
        String structuredNameWhere = MobiComDatabaseHelper.USERID + " =?";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(CONTACT, null, structuredNameWhere, new String[]{id}, null, null, null);
        Contact contact = null;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                contact = getContact(cursor);
            }
            cursor.close();
        }
        dbHelper.close();
        return contact;

    }

    public void updateContact(Contact contact) {
        ContentValues contentValues = prepareContactValues(contact);
        dbHelper.getWritableDatabase().update(CONTACT, contentValues, MobiComDatabaseHelper.USERID + "=?", new String[]{contact.getUserId()});
        dbHelper.close();
    }

    public void updateConnectedOrDisconnectedStatus(String userId, Date date, boolean connected) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MobiComDatabaseHelper.CONNECTED, connected ? 1 : 0);
        contentValues.put(MobiComDatabaseHelper.LAST_SEEN_AT_TIME, date.getTime());

        try {
            dbHelper.getWritableDatabase().update(CONTACT, contentValues, MobiComDatabaseHelper.USERID + "=?", new String[]{userId});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }

    public void updateLastSeenTimeAt(String userId, long lastSeenTime) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MobiComDatabaseHelper.LAST_SEEN_AT_TIME, lastSeenTime);
            dbHelper.getWritableDatabase().update(CONTACT, contentValues, MobiComDatabaseHelper.USERID + "=?", new String[]{userId});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }

    public void addContact(Contact contact) {
        if (TextUtils.isEmpty(contact.getContactNumber())) {
            contact.setContactNumber(contact.getUserId());
        }
        ContentValues contentValues = prepareContactValues(contact);
        dbHelper.getWritableDatabase().insert(CONTACT, null, contentValues);
        dbHelper.close();
    }

    public ContentValues prepareContactValues(Contact contact) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MobiComDatabaseHelper.FULL_NAME, contact.getFullName());
        contentValues.put(MobiComDatabaseHelper.CONTACT_NO, contact.getContactNumber());
        if (!TextUtils.isEmpty(contact.getImageURL())) {
            contentValues.put(MobiComDatabaseHelper.CONTACT_IMAGE_URL, contact.getImageURL());
        }
        if (!TextUtils.isEmpty(contact.getLocalImageUrl())) {
            contentValues.put(MobiComDatabaseHelper.CONTACT_IMAGE_LOCAL_URI, contact.getLocalImageUrl());
        }
        contentValues.put(MobiComDatabaseHelper.USERID, contact.getUserId());
        if (!TextUtils.isEmpty(contact.getEmailId())) {
            contentValues.put(MobiComDatabaseHelper.EMAIL, contact.getEmailId());
        }
        if (!TextUtils.isEmpty(contact.getApplicationId())) {
            contentValues.put(MobiComDatabaseHelper.APPLICATION_ID, contact.getApplicationId());
        }

        contentValues.put(MobiComDatabaseHelper.CONNECTED, contact.isConnected() ? 1 : 0);
        if (contact.getLastSeenAt() != 0) {
            contentValues.put(MobiComDatabaseHelper.LAST_SEEN_AT_TIME, contact.getLastSeenAt());
        }
        return contentValues;
    }

    public void addAllContact(List<Contact> contactList) {
        for (Contact contact : contactList) {
            addContact(contact);
        }
    }

    public void deleteContact(Contact contact) {
        deleteContactById(contact.getUserId());
    }

    public void deleteContactById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(CONTACT, "userId=?", new String[]{id});
        dbHelper.close();
    }

    public void deleteAllContact(List<Contact> contacts) {
        for (Contact contact : contacts) {
            deleteContact(contact);
        }
    }

}
