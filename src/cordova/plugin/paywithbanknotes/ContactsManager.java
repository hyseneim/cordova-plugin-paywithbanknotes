package cordova.plugin.paywithbanknotes;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactsManager {

    private static final String TAG = "ContactsManager";

    private static final String MIMETYPE =
            "vnd.android.cursor.item/it.altran.ionic.banknotes.premium";

    public static void addContact(Context context, MyContact contact) {
        ContentResolver resolver = context.getContentResolver();

        ArrayList<ContentProviderOperation> ops =
                new ArrayList<>();

        ops.add(ContentProviderOperation.newInsert(
                addCallerIsSyncAdapterParameter(RawContacts.CONTENT_URI, true))
                .withValue(RawContacts.ACCOUNT_NAME, AccountGeneral.ACCOUNT_NAME)
                .withValue(RawContacts.ACCOUNT_TYPE, AccountGeneral.ACCOUNT_TYPE)
                .build());

        ops.add(ContentProviderOperation.newInsert(
                addCallerIsSyncAdapterParameter(Settings.CONTENT_URI, true))
                .withValue(RawContacts.ACCOUNT_NAME, AccountGeneral.ACCOUNT_NAME)
                .withValue(RawContacts.ACCOUNT_TYPE, AccountGeneral.ACCOUNT_TYPE)
                .withValue(Settings.UNGROUPED_VISIBLE, 1)
                .build());

        ops.add(ContentProviderOperation.newInsert(
                addCallerIsSyncAdapterParameter(Data.CONTENT_URI, true))
                .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.GIVEN_NAME, contact.name)
                .withValue(StructuredName.FAMILY_NAME, contact.lastName)
                .build());

        ops.add(ContentProviderOperation.newInsert(
                addCallerIsSyncAdapterParameter(Data.CONTENT_URI, true))
                .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                .withValue(Data.MIMETYPE, MIMETYPE)
                .withValue(Data.DATA1, "Paga con Bank-Notes")
                .withValue(Data.DATA2, "Paga con Bank-Notes")
                .withValue(Data.DATA3, "Paga con Bank-Notes")
                .withValue(Data.DATA7, contact.iban)
                .withValue(Data.DATA8, contact.displayName)
                .build());

        ops.add(ContentProviderOperation.newInsert(
                addCallerIsSyncAdapterParameter(Data.CONTENT_URI, true))
                .withValue(Data.RAW_CONTACT_ID, contact.id)
                .withValue(Data.MIMETYPE, Note.CONTENT_ITEM_TYPE)
                .withValue(Note.NOTE, contact.iban)
                .build());

        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private static Uri addCallerIsSyncAdapterParameter(
            Uri uri, boolean isSyncOperation) {
        if (isSyncOperation) {
            return uri.buildUpon()
                    .appendQueryParameter(
                            ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                    .build();
        }
        return uri;
    }

    public static MyContact findContactById(Context context, String label, String id, boolean findByAccountType) {
        MyContact result = null;

        StringBuilder filter = new StringBuilder(label + "= ?");

        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(id);

        if (findByAccountType) {
            filter.append(" AND ");
            filter.append(RawContacts.ACCOUNT_TYPE);
            filter.append(" = ?");
            selectionArgs.add(AccountGeneral.ACCOUNT_TYPE);
        }
        else {
            filter.append(" AND ");
            filter.append(Data.MIMETYPE);
            filter.append(" = ?");
            selectionArgs.add(StructuredName.CONTENT_ITEM_TYPE);
        }

        Cursor cursor =
                context.getContentResolver().query(Data.CONTENT_URI, new String[]{
                                Data.RAW_CONTACT_ID, Data.DISPLAY_NAME, Data.MIMETYPE,
                                Data.CONTACT_ID,
                                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME
                        },
                        filter.toString(),
                        selectionArgs.toArray(new String[0]),
                        null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Log.i(TAG, "Raw contact ID: " + cursor.getString(0));
                String displayName = cursor.getString(1);
                Log.i(TAG, "Display name: " + displayName);
                Log.i(TAG, "Mimetype: " + cursor.getString(2));
                String realId = cursor.getString(3);
                Log.i(TAG, "Contact ID: " + realId);
                String givenName = cursor.getString(4);
                String familyName = cursor.getString(5);
                Log.i(TAG, "GivenName: " + givenName);
                Log.i(TAG, "FamilyName: " + familyName);
                result = new MyContact(
                    cursor.getInt(0), givenName, familyName, 
                    realId, displayName
                );
            } while (cursor.moveToNext());
        }

        return result;
    }

    private static boolean isAdd(Context context, String contactId) {
        boolean isAdd = true;

        StringBuilder filter = new StringBuilder(Data.CONTACT_ID + " = ?");

        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(contactId);

        filter.append(" AND ");
        filter.append(Data.MIMETYPE);
        filter.append(" = ?");
        selectionArgs.add(MIMETYPE);

        Cursor cursor =
                context.getContentResolver().query(Data.CONTENT_URI, 
                        new String[] {
                            Data.DATA7,
                            Data.DATA8
                        },
                        filter.toString(),
                        selectionArgs.toArray(new String[0]),
                        null);

        if (cursor != null && cursor.moveToFirst()) {
            String bankNotesContactId = cursor.getString(0);
            String bankNotesIban = cursor.getString(1);
            Log.i(TAG, "bankNotesContactId: " + bankNotesContactId);
            Log.i(TAG, "bankNotesIban: " + bankNotesIban);
            if (bankNotesContactId != null && bankNotesIban != null) {
                isAdd = false;
            }
        }

        return isAdd;
    }

    private static void updateContact(Context context, MyContact contact) {
        ContentResolver resolver = context.getContentResolver();

        ArrayList<ContentProviderOperation> ops =
                new ArrayList<>();

        ops.add(ContentProviderOperation.newUpdate(
                addCallerIsSyncAdapterParameter(Data.CONTENT_URI, true))
                .withSelection(Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?", 
                new String[] {
                    String.valueOf(contact.realId),
                    MIMETYPE
                })
                .withValue(Data.RAW_CONTACT_ID, contact.id)
                .withValue(Data.MIMETYPE, MIMETYPE)
                .withValue(Data.DATA1, "Paga con Bank-Notes")
                .withValue(Data.DATA2, "Paga con Bank-Notes")
                .withValue(Data.DATA3, "Paga con Bank-Notes")
                .withValue(Data.DATA7, contact.iban)
                .withValue(Data.DATA8, contact.displayName)
                .build());

        ops.add(ContentProviderOperation.newUpdate(
                addCallerIsSyncAdapterParameter(Data.CONTENT_URI, true))
                .withSelection(Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?", 
                new String[] {
                    String.valueOf(contact.realId),
                    Note.CONTENT_ITEM_TYPE
                })
                .withValue(Data.RAW_CONTACT_ID, contact.id)
                .withValue(Data.MIMETYPE, Note.CONTENT_ITEM_TYPE)
                .withValue(Note.NOTE, contact.iban)
                .build());

        try {
            ContentProviderResult[] results = context.getContentResolver().applyBatch(
                    ContactsContract.AUTHORITY, ops);
            for (ContentProviderResult result : results) {
                Log.i(TAG, result.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static void updateContactById(Context context, String id, String iban) {
        Log.i(TAG, "updateContact with id: " + id);

        MyContact contact = findContactById(context, Data.RAW_CONTACT_ID, id, false);

        if (contact != null) {
            Log.i(TAG, "Contact found");
            contact.iban = iban;

            if (isAdd(context, contact.realId)) {
                Log.i(TAG, "Add a contact");

                addContact(context, contact);

                MyContact addedContact = findContactById(context, Data.DATA8, iban, true);
    
                if (addedContact != null) {
                    Log.i(TAG, "Found added contact");
    
                    try {
                        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
    
                        ops.add(ContentProviderOperation.newUpdate(ContactsContract.AggregationExceptions.CONTENT_URI)
                                .withValue(ContactsContract.AggregationExceptions.TYPE, ContactsContract.AggregationExceptions.TYPE_KEEP_TOGETHER)
                                .withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID1, addedContact.id)
                                .withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID2, contact.id)
                                .build());
    
                        ContentProviderResult[] results = context.getContentResolver().applyBatch(
                                ContactsContract.AUTHORITY, ops);
                        for (ContentProviderResult result : results) {
                            Log.i(TAG, result.toString());
                        }
                    }
                    catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }
            else {
                Log.i(TAG, "Update a contact");

                updateContact(context, contact);
            }

        } else {
            Log.i(TAG, "Contact not found");
        }
    }

}
